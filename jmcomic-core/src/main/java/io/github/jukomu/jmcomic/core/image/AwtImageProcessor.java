package io.github.jukomu.jmcomic.core.image;

import io.github.jukomu.jmcomic.api.exception.JmComicException;
import io.github.jukomu.jmcomic.api.model.JmImage;
import io.github.jukomu.jmcomic.core.image.spi.ImageProcessor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static io.github.jukomu.jmcomic.core.crypto.JmImageTool.calculateNumSegments;
import static io.github.jukomu.jmcomic.core.crypto.JmImageTool.getFormatName;

/**
 * @author JUKOMU
 * @Description: ImageProcessor 的默认实现，基于 Java SE 的 AWT 和 ImageIO
 * @Project: jmcomic-api-java
 * @Date: 2025/11/4
 */
public class AwtImageProcessor implements ImageProcessor {
    @Override
    public byte[] decryptImage(byte[] imageData, JmImage image) throws JmComicException {
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
}
