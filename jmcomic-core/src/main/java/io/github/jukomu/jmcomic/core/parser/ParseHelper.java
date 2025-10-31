package io.github.jukomu.jmcomic.core.parser;

import io.github.jukomu.jmcomic.api.exception.ParseResponseException;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author JUKOMU
 * @Description: 内部解析辅助工具类，提供通用的解析方法
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class ParseHelper {

    private ParseHelper() {
    }

    /**
     * 从一段文本中用正则表达式提取第一个匹配组的内容
     *
     * @throws ParseResponseException 如果没有找到匹配项
     */
    static String extractByRegex(String text, Pattern pattern, String fieldName) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        throw new ParseResponseException("Failed to parse " + fieldName + ": Regex did not match. Pattern: " + pattern);
    }

    /**
     * 从 Jsoup 元素中安全地选择第一个匹配的元素并提取其文本
     *
     * @throws ParseResponseException 如果选择器没有匹配到任何元素
     */
    static String selectFirstText(Element root, String cssSelector, String fieldName) {
        return Optional.ofNullable(root.selectFirst(cssSelector))
                .map(Element::text)
                .map(String::trim)
                .orElse(null);
    }

    /**
     * 从 Jsoup 元素中选择所有匹配的元素，提取它们的文本并返回一个列表
     */
    static List<String> selectAllText(Element root, String cssSelector) {
        return root.select(cssSelector)
                .stream()
                .map(Element::text)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * 用于 orElse(null) 的情况
     */
    static String selectFirstTextOrNull(Element root, String cssSelector) {
        return Optional.ofNullable(root.selectFirst(cssSelector))
                .map(Element::text)
                .map(String::trim)
                .orElse(null);
    }

    static int parseIntOrDefault(String text, java.util.function.Supplier<String> fallbackSupplier) {
        String primaryText = text;
        // 如果主文本为空，则尝试从备用源获取
        if (primaryText == null || primaryText.isEmpty() || primaryText.replaceAll("[^0-9]", "").isEmpty()) {
            if (fallbackSupplier != null) {
                primaryText = fallbackSupplier.get();
            }
        }
        return parseIntOrDefault(primaryText, 0); // 复用已有的解析逻辑
    }

    public static String extractTextAfterKey(Element panel, String key) {
        if (panel == null) return null;
        String text = panel.text();
        int keyIndex = text.indexOf(key);
        if (keyIndex == -1) return null;
        // ... 在这里可以添加更复杂的逻辑来提取后面的数字
        return text.substring(keyIndex + key.length()).trim().split(" ")[0];
    }

    /**
     * 将代表数字的字符串转换为 int，如果失败则返回默认值
     */
    static int parseIntOrDefault(String text, int defaultValue) {
        if (text == null || text.isEmpty()) {
            return defaultValue;
        }
        try {
            // 移除可能存在的逗号等非数字字符
            return Integer.parseInt(text.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
