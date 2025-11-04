package io.github.jukomu.jmcomic.core.net.provider;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author JUKOMU
 * @Description: 内部类，负责管理和选择域名
 * 它是有状态的，会跟踪每个域名的连续失败次数，并优先选择状态最佳的域名
 * 这个类的实例将与每个 JmClient 实例一一对应，确保域名状态隔离
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class JmDomainManager {

    private final CopyOnWriteArrayList<String> domains;
    private final ConcurrentHashMap<String, AtomicInteger> failureCounts = new ConcurrentHashMap<>();

    public JmDomainManager(List<String> domains) {
        this.domains = new CopyOnWriteArrayList<>(domains);
        domains.forEach(domain -> failureCounts.putIfAbsent(domain, new AtomicInteger(0)));
    }

    /**
     * 根据当前失败次数选择一个最佳域名。
     *
     * @return 状态最佳的域名。如果没有可用域名则返回 null。
     */
    public String getBestDomain() {
        return domains.stream()
                .min(Comparator.comparingInt(domain -> failureCounts.get(domain).get()))
                .orElse(null);
    }

    /**
     * 报告某个域名请求成功。
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
     * 报告某个域名请求失败。
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
     *
     * @return 一个包含域名及其失败次数的Map。
     */
    public Map<String, Integer> getDomainStates() {
        return domains.stream()
                .collect(Collectors.toMap(
                        domain -> domain,
                        domain -> failureCounts.get(domain).get()
                ));
    }

    /**
     * 更新域名列表。
     *
     * @param newDomains 最新的域名列表。
     */
    public void updateDomains(List<String> newDomains) {
        // 将新域名添加到列表中
        domains.clear();
        domains.addAll(newDomains);
        failureCounts.clear();
        domains.forEach(domain -> failureCounts.putIfAbsent(domain, new AtomicInteger(0)));
    }

    public CopyOnWriteArrayList<String> getDomains() {
        return domains;
    }
}
