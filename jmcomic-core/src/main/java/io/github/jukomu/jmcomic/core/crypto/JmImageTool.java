package io.github.jukomu.jmcomic.core.crypto;

import io.github.jukomu.jmcomic.api.exception.JmComicException;
import io.github.jukomu.jmcomic.api.model.JmImage;
import io.github.jukomu.jmcomic.core.constant.JmConstants;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author JUKOMU
 * @Description: 内部工具类，负责处理JMComic的图片加解密逻辑
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class JmImageTool {

    private JmImageTool() {
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
        // 根据图片元数据计算分割数
        int numSegments = calculateNumSegments(Long.parseLong(image.scrambleId()), Long.parseLong(image.photoId()), image.getFilenameWithoutSuffix());

        // 如果分割数为0，则图片无需重组，直接返回原始数据
        if (numSegments == 0) {
            return imageData;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData)) {
            BufferedImage originalImage = ImageIO.read(bais);
            if (originalImage == null) {
                throw new JmComicException("Failed to read image data. The data may be corrupted or in an unsupported format.");
            }

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            // 如果图片高度不足以进行分割，也直接返回原始数据，以避免错误
            if (height < numSegments) {
                return imageData;
            }

            // 创建一个新的空白图像用于存放重组后的结果
            BufferedImage decryptedImage = new BufferedImage(width, height, originalImage.getType());

            int segmentHeight = height / numSegments;
            int remainder = height % numSegments;

            int currentY = 0;
            for (int i = 0; i < numSegments; i++) {
                int ySrc;
                int hSrc = segmentHeight;

                if (i == 0) {
                    hSrc += remainder;
                    ySrc = height - hSrc;
                } else {
                    ySrc = height - (segmentHeight * (i + 1)) - remainder;
                }

                // 从原图中裁剪出一块
                BufferedImage segment = originalImage.getSubimage(0, ySrc, width, hSrc);

                // 将裁剪出的块粘贴到新图的正确位置
                decryptedImage.createGraphics().drawImage(segment, 0, currentY, null);

                currentY += hSrc;
            }

            // 将重组后的 BufferedImage 转换回 byte[]
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                String format = getFormatName(image.filename());
                ImageIO.write(decryptedImage, format, baos);
                return baos.toByteArray();
            }
        } catch (IOException e) {
            throw new JmComicException("An I/O error occurred during image decryption", e);
        } catch (Exception e) {
            // 捕获所有可能的运行时异常，例如 getSubimage 时的尺寸错误
            throw new JmComicException("An unexpected error occurred during image decryption for image: " + image.getTag(), e);
        }
    }

    /**
     * 根据 scrambleId, photoId, 和图片文件名计算图片被切割的块数
     *
     * @param scrambleId            本子的 scramble ID
     * @param photoId               章节的 ID (在Python代码中被称为 aid)
     * @param filenameWithoutSuffix 不带后缀的文件名
     * @return 分割数 (如果无需分割则为0)
     */
    private static int calculateNumSegments(long scrambleId, long photoId, String filenameWithoutSuffix) {
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
    private static String getFormatName(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == filename.length() - 1) {
            return "jpeg"; // 默认返回jpeg
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }
}
