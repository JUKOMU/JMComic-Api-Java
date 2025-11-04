package io.github.jukomu.jmcomic.core.image.spi;

import io.github.jukomu.jmcomic.api.exception.JmComicException;
import io.github.jukomu.jmcomic.api.model.JmImage;


/**
 * @author JUKOMU
 * @Description: 图片处理器的服务提供者接口 (SPI)
 * 这个接口抽象了图片解码、重组和编码的功能，以解耦对特定图形库
 * @Project: jmcomic-api-java
 * @Date: 2025/11/4
 */
public interface ImageProcessor {

    /**
     * 对JMComic的图片数据进行解密（重组）
     * 如果根据算法判断图片无需重组，将直接返回原始数据
     *
     * @param imageData 原始的、未解密的图片字节数组
     * @param image     包含解密所需元数据的 JmImage 对象
     * @return 解密（重组）后的图片字节数组
     * @throws JmComicException 如果处理过程中发生错误
     */
    byte[] decryptImage(byte[] imageData, JmImage image) throws JmComicException;
}