package io.github.jukomu.jmcomic.core.crypto;

import io.github.jukomu.jmcomic.api.model.JmImage;
import io.github.jukomu.jmcomic.core.constant.JmConstants;
import io.github.jukomu.jmcomic.core.image.spi.ImageProcessor;

import java.util.ServiceLoader;

/**
 * @author JUKOMU
 * @Description: 内部工具类，负责处理JMComic的图片加解密逻辑
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class JmImageTool {
    private static ImageProcessor customProcessor = loadFirstImageProcessor();

    private JmImageTool() {
    }

    private static ImageProcessor loadFirstImageProcessor() {
        ServiceLoader<ImageProcessor> loader = ServiceLoader.load(ImageProcessor.class);
        for (ImageProcessor spi : loader) {
            return spi;
        }
        try {
            Class<?> clazz = Class.forName("io.github.jukomu.jmcomic.core.image.AwtImageProcessor");
            return (ImageProcessor) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("当前环境不支持 AWT，且未导入 Android Support 模块！", e);
        }
    }

    /**
     * 对JMComic的图片数据进行解密（重组）
     * 如果根据算法判断图片无需重组，将直接返回原始数据
     *
     * @param imageData 原始的、未解密的图片字节数组
     * @param image     包含解密所需元数据的 JmImage 对象
     * @return 解密（重组）后的图片字节数组
     */
    public static byte[] decryptImage(byte[] imageData, JmImage image) {
        ImageProcessor processor = customProcessor;
        return processor.decryptImage(imageData, image);
    }

    /**
     * 根据 scrambleId, photoId, 和图片文件名计算图片被切割的块数
     *
     * @param scrambleId            本子的 scramble ID
     * @param photoId               章节的 ID (在Python代码中被称为 aid)
     * @param filenameWithoutSuffix 不带后缀的文件名
     * @return 分割数 (如果无需分割则为0)
     */
    public static int calculateNumSegments(long scrambleId, long photoId, String filenameWithoutSuffix) {
        if (photoId < scrambleId) {
            return 0;
        }

        if (photoId < JmConstants.SCRAMBLE_268850) {
            return 10;
        }

        int x = (photoId < JmConstants.SCRAMBLE_421926) ? 10 : 8;
        String s = photoId + filenameWithoutSuffix;
        String md5 = JmCryptoTool.md5Hex(s);

        // 获取MD5哈希值的最后一个字符的ASCII码
        char lastChar = md5.charAt(md5.length() - 1);
        int num = lastChar;

        num %= x;
        num = num * 2 + 2;

        return num;
    }

    /**
     * 从文件名中提取图片格式，用于 ImageIO.write
     *
     * @param filename 包含后缀的文件名
     * @return 图片格式字符串 (例如 "jpg", "png")
     */
    public static String getFormatName(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == filename.length() - 1) {
            return "jpeg"; // 默认返回jpeg
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }
}
