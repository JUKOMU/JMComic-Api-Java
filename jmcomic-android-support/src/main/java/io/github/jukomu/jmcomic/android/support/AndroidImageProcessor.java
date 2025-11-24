package io.github.jukomu.jmcomic.android.support;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import io.github.jukomu.jmcomic.api.exception.JmComicException;
import io.github.jukomu.jmcomic.api.model.JmImage;
import io.github.jukomu.jmcomic.core.crypto.JmImageTool;
import io.github.jukomu.jmcomic.core.image.spi.ImageProcessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static io.github.jukomu.jmcomic.core.crypto.JmImageTool.calculateNumSegments;

/**
 * @author JUKOMU
 * @Description: ImageProcessor 的一个实现，基于 Android平台的 Bitmap
 * @Project: jmcomic-api-java
 * @Date: 2025/11/25
 */
public class AndroidImageProcessor implements ImageProcessor {

    @Override
    public byte[] decryptImage(byte[] imageData, JmImage image) throws JmComicException {
        // 根据图片元数据计算分割数
        int numSegments = calculateNumSegments(Long.parseLong(image.scrambleId()), Long.parseLong(image.photoId()), image.getFilenameWithoutSuffix());

        // 如果分割数为0，则图片无需重组，直接返回原始数据
        if (numSegments == 0) {
            return imageData;
        }
        try {
            Bitmap originalBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            if (originalBitmap == null) {
                throw new JmComicException("Failed to read image data. The data may be corrupted or in an unsupported format.");
            }

            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();

            if (height < numSegments) {
                originalBitmap.recycle();
                return imageData;
            }

            Bitmap decryptedBitmap = Bitmap.createBitmap(width, height, originalBitmap.getConfig());
            Canvas canvas = new Canvas(decryptedBitmap);

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

                // 定义源矩形和目标矩形
                Rect srcRect = new Rect(0, ySrc, width, ySrc + hSrc);
                Rect destRect = new Rect(0, currentY, width, currentY + hSrc);

                canvas.drawBitmap(originalBitmap, srcRect, destRect, null);
                currentY += hSrc;
            }

            // 将重组后的 Bitmap 编码回字节数组
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                Bitmap.CompressFormat format = getCompressFormat(JmImageTool.getFormatName(image.filename()));

                decryptedBitmap.compress(format, 100, baos);

                // 及时释放Bitmap内存
                originalBitmap.recycle();
                decryptedBitmap.recycle();

                return baos.toByteArray();
            }
        } catch (IOException e) {
            throw new JmComicException("An I/O error occurred during image decryption", e);
        } catch (Exception e) {
            // 捕获所有可能的运行时异常，例如 getSubimage 时的尺寸错误
            throw new JmComicException("An unexpected error occurred during image decryption for image: " + image.getTag(), e);
        }
    }

    private Bitmap.CompressFormat getCompressFormat(String formatName) {
        return switch (formatName.toLowerCase()) {
            case "jpeg", "jpg" -> Bitmap.CompressFormat.JPEG;
            case "png" -> Bitmap.CompressFormat.PNG;
            case "webp" -> Bitmap.CompressFormat.WEBP;
            default -> Bitmap.CompressFormat.JPEG;
        };
    }
}
