package io.github.jukomu.jmcomic.core.net.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 管理和选择域名。跟踪每个域名的失败次数，优先选择状态最佳的域名。
 * 支持探活预处理：在初始化时探测所有域名可达性，排除死域名后
 * 再开放请求，避免重试次数被永久不可达的域名消耗。
 *
 * @author JUKOMU
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class JmDomainManager {

    private static final Logger logger = LoggerFactory.getLogger(JmDomainManager.class);

    /**
     * 用于标记探活失败的域名。取 Integer.MAX_VALUE/2 以避免溢出，同时远大于正常失败计数
     */
    static final int DEAD_MARK = Integer.MAX_VALUE / 2;

    private final CopyOnWriteArrayList<String> domains;
    private final ConcurrentHashMap<String, AtomicInteger> failureCounts = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;
    private volatile CountDownLatch initLatch = new CountDownLatch(1);

    /**
     * 后台复探定时器，由 startPeriodicProbe 创建
     */
    private volatile ScheduledExecutorService probeScheduler;

    public JmDomainManager(List<String> domains) {
        this.domains = new CopyOnWriteArrayList<>(domains);
        domains.forEach(domain -> failureCounts.putIfAbsent(domain, new AtomicInteger(0)));
    }

    /**
     * 根据当前失败次数选择一个最佳域名。
     * 只有初始化完成后才会返回结果，确保探活已执行。
     *
     * @return 状态最佳的域名。如果没有可用域名则返回 null。
     */
    public String getBestDomain() {
        blockUntilInitialized();
        return domains.stream()
                .min(Comparator.comparingInt(domain -> failureCounts.get(domain).get()))
                .orElse(null);
    }

    /**
     * 报告某个域名请求成功。将失败计数归零。
     *
     * @param domain 请求成功的域名。
     */
    public void reportSuccess(String domain) {
        AtomicInteger count = failureCounts.get(domain);
        if (count != null) {
            count.set(0);
        }
    }

    /**
     * 报告某个域名请求失败，增加其失败计数值。
     *
     * @param domain 请求失败的域名。
     */
    public void reportFailure(String domain) {
        AtomicInteger count = failureCounts.get(domain);
        if (count != null) {
            count.incrementAndGet();
        }
    }

    /**
     * 获取所有域名的当前状态，用于调试。
     */
    public Map<String, Integer> getDomainStates() {
        blockUntilInitialized();
        return domains.stream()
                .collect(Collectors.toMap(
                        domain -> domain,
                        domain -> failureCounts.get(domain).get()
                ));
    }

    /**
     * 更新域名列表并清空所有失败计数。
     */
    public void updateDomains(List<String> newDomains) {
        domains.clear();
        domains.addAll(newDomains);
        failureCounts.clear();
        domains.forEach(domain -> failureCounts.putIfAbsent(domain, new AtomicInteger(0)));
    }

    public CopyOnWriteArrayList<String> getDomains() {
        return domains;
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 设置初始化状态。初始化完成时释放阻塞的 getBestDomain() 调用。
     */
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
        if (initialized) {
            if (initLatch.getCount() > 0) {
                initLatch.countDown();
            }
        } else {
            if (initLatch.getCount() == 0) {
                initLatch = new CountDownLatch(1);
            }
        }
    }

    // == 探活相关 ==

    /**
     * 并行探测所有域名的可达性。
     * <p>
     * 对每个域名调用 {@link DomainProbe#isReachable(String)}。
     * 可达的域名计数归零，不可达的域名计数设为 {@link #DEAD_MARK}。
     * <p>
     * 如果所有域名都不可达（全死），则将所有计数重置为 0，
     * 回退到未探活状态，让后续实际请求自行判断。
     *
     * @param probe 探活实现（由调用方注入，如 HTTP HEAD 请求）
     */
    public void probeAllDomains(DomainProbe probe) {
        if (domains.isEmpty()) {
            logger.warn("域名列表为空，跳过探活");
            return;
        }

        logger.info("开始并行探活 {} 个域名...", domains.size());
        long startTime = System.currentTimeMillis();

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (String domain : domains) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    boolean reachable = probe.isReachable(domain);
                    AtomicInteger count = failureCounts.get(domain);
                    if (count != null) {
                        if (reachable) {
                            count.set(0);
                            logger.debug("域名 {} 探活成功", domain);
                        } else {
                            count.set(DEAD_MARK);
                            logger.warn("域名 {} 探活失败，标记为不可达", domain);
                        }
                    }
                } catch (Exception e) {
                    AtomicInteger count = failureCounts.get(domain);
                    if (count != null) {
                        count.set(DEAD_MARK);
                    }
                    logger.warn("域名 {} 探活异常: {}", domain, e.getMessage());
                }
            });
            futures.add(future);
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            logger.warn("探活过程中出现异常，部分域名可能未完成探测", e);
        }

        // 全死降级检查
        boolean allDead = domains.stream()
                .allMatch(d -> failureCounts.get(d).get() >= DEAD_MARK);
        if (allDead) {
            logger.warn("所有域名探活失败，回退到未探活状态（重置所有计数为 0）");
            domains.forEach(d -> failureCounts.get(d).set(0));
        }

        long elapsed = System.currentTimeMillis() - startTime;
        long aliveCount = domains.stream()
                .filter(d -> failureCounts.get(d).get() < DEAD_MARK)
                .count();
        logger.info("探活完成 (耗时 {}ms): {}/{} 个域名可达", elapsed, aliveCount, domains.size());
    }

    /**
     * 启动后台定期复探任务。
     * <p>
     * 每隔指定时间，对所有标记为 DEAD_MARK 的域名重新探活。
     * 如果某域名恢复可达，则将其失败计数归零，使其自动回到可用池。
     *
     * @param probe      探活实现
     * @param intervalMs 复探间隔（毫秒）
     */
    public void startPeriodicProbe(DomainProbe probe, long intervalMs) {
        if (probeScheduler != null) {
            return; // 已经启动
        }
        probeScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "domain-probe-scheduler");
            t.setDaemon(true);
            return t;
        });

        probeScheduler.scheduleWithFixedDelay(() -> {
            try {
                List<String> deadDomains = domains.stream()
                        .filter(d -> failureCounts.get(d).get() >= DEAD_MARK)
                        .collect(Collectors.toList());

                if (deadDomains.isEmpty()) {
                    return;
                }

                logger.debug("后台复探 {} 个死域名...", deadDomains.size());
                for (String domain : deadDomains) {
                    try {
                        if (probe.isReachable(domain)) {
                            failureCounts.get(domain).set(0);
                            logger.info("域名 {} 已恢复，重新加入可用池", domain);
                        }
                    } catch (Exception e) {
                        logger.debug("后台复探域名 {} 异常: {}", domain, e.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.warn("后台复探任务异常", e);
            }
        }, intervalMs, intervalMs, TimeUnit.MILLISECONDS);

        logger.info("后台域名复探已启动，间隔 {}ms", intervalMs);
    }

    /**
     * 关闭后台复探定时器。
     */
    public void shutdown() {
        if (probeScheduler != null && !probeScheduler.isShutdown()) {
            probeScheduler.shutdown();
            logger.info("后台域名复探已关闭");
        }
    }

    // == 内部方法 ==

    private void blockUntilInitialized() {
        if (this.initialized) return;
        try {
            CountDownLatch latch = this.initLatch;
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Wait for initialization was interrupted", e);
        }
    }
}
