package io.github.jukomu.jmcomic.desktop;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import io.github.jukomu.jmcomic.api.net.CloudflareChallengeSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;


/**
 * @author JUKOMU
 * @Description: CloudflareChallengeSolver 的一个实现，基于 Playwright
 * @Project: jmcomic-api-java
 * @Date: 2026-02-14
 */
public class DesktopPlaywrightSolver implements CloudflareChallengeSolver {
    private static final Logger logger = LoggerFactory.getLogger(DesktopPlaywrightSolver.class);

    @Override
    public Solution solve(String url) {
        logger.info("启动 Playwright 浏览器以进行 Cloudflare 验证...");

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    // 必须显示 UI 让用户操作
                    new BrowserType.LaunchOptions().setHeadless(false)
            );
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            try {
                logger.info("正在导航至: {}", url);
                page.navigate(url);

                logger.warn("请在弹出的浏览器窗口中完成人机验证。超时时间: 3分钟");

                // 等待 cf_clearance cookie 出现
                page.waitForCondition(() -> {
                    for (Cookie cookie : context.cookies()) {
                        if ("cf_clearance".equals(cookie.name)) {
                            return true;
                        }
                    }
                    return false;
                }, new Page.WaitForConditionOptions().setTimeout(180_000)); // 3分钟超时

                logger.info("验证成功！正在提取凭证...");

                // 提取所有 cookies
                String cookieStr = context.cookies().stream()
                        .map(c -> c.name + "=" + c.value)
                        .collect(Collectors.joining("; "));

                // 提取浏览器的 User-Agent，这非常关键
                String userAgent = (String) page.evaluate("() => navigator.userAgent");

                return new Solution(cookieStr, userAgent);

            } catch (Exception e) {
                logger.error("Cloudflare 验证失败或超时", e);
                return null;
            } finally {
                browser.close();
            }
        }
    }
}
