package io.github.jukomu.jmcomic.core.net.provider;

/**
 * 域名可达性探活接口。
 * 由 JmDomainManager 回调，不直接依赖 HTTP 层。
 *
 * @author JUKOMU
 * @Project: jmcomic-api-java
 * @Date: 2026/4/26
 */
@FunctionalInterface
public interface DomainProbe {

    /**
     * 探测指定域名是否可达。
     *
     * @param domain 待探测的域名
     * @return true 表示域名可达（或至少响应了探活请求）
     */
    boolean isReachable(String domain);
}
