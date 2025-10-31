package io.github.jukomu.jmcomic.core.util;

import java.util.regex.Pattern;

/**
 * @author JUKOMU
 * @Description: 内部文件操作相关的工具类
 * @Project: jmcomic-api-java
 * @Date: 2025/10/31
 */
public final class FileUtils {

    private FileUtils() {
        // 防止实例化
    }

    /**
     * 定义了一个匹配在主流操作系统（特别是Windows）中非法的或不推荐的文件名字符的正则表达式
     * 包括： \ / : * ? " < > | 以及所有控制字符（例如换行、制表符）
     * \\u0000 是 null 字符。
     */
    private static final Pattern ILLEGAL_CHARACTERS_PATTERN = Pattern.compile("[\\\\/:*?\"<>|\\p{Cntrl}]");

    /**
     * "净化"一个字符串，使其可以安全地用作文件名或文件夹名。
     * 它会移除所有在主流操作系统中非法的字符。
     *
     * @param input       原始的、可能包含非法字符的字符串。
     * @param replacement 用于替换非法字符的字符串。不能为空。
     * @return 净化后的字符串。
     */
    public static String sanitizeFilename(String input, String replacement) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        if (replacement == null) {
            throw new IllegalArgumentException("Replacement string cannot be null.");
        }
        return ILLEGAL_CHARACTERS_PATTERN.matcher(input).replaceAll(replacement);
    }

    /**
     * "净化"一个字符串，使其可以安全地用作文件名或文件夹名。
     * 非法字符将被替换为下划线 "_"。
     *
     * @param input 原始的、可能包含非法字符的字符串。
     * @return 净化后的字符串。
     */
    public static String sanitizeFilename(String input) {
        return sanitizeFilename(input, "_");
    }
}
