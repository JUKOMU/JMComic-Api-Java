package io.github.jukomu.jmcomic.core.crypto;

import io.github.jukomu.jmcomic.api.exception.JmComicException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @author JUKOMU
 * @Description: 内部工具类，负责处理JMComic API的加密和解密逻辑
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public final class JmCryptoTool {

    private JmCryptoTool() {
    }

    private static final String ALGORITHM_AES_ECB = "AES/ECB/PKCS5Padding";
    private static final String ALGORITHM_MD5 = "MD5";

    /**
     * 生成请求API所需的 token 和 token_param
     *
     * @param timestamp  当前时间戳 (秒)
     * @param secret     加密密钥，例如 JmConstants.APP_TOKEN_SECRET
     * @param appVersion APP版本号，例如 JmConstants.APP_VERSION
     * @return 包含 token 和 token_param 的字符串数组
     */
    public static String[] generateToken(String timestamp, String secret, String appVersion) {
        String tokenParam = timestamp + "," + appVersion;
        String token = md5Hex(timestamp + secret);
        return new String[]{token, tokenParam};
    }

    /**
     * 解密API返回的加密数据
     *
     * @param encryptedData base64编码的加密数据
     * @param timestamp     用于生成密钥的时间戳
     * @param secret        加密密钥，例如 JmConstants.APP_DATA_SECRET
     * @return 解密后的JSON字符串
     */
    public static String decryptApiResponse(String encryptedData, String timestamp, String secret) {
        try {
            // 1. 生成AES密钥
            byte[] key = md5Hex(timestamp + secret).getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

            // 2. Base64解码
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);

            // 3. AES-ECB解密
            Cipher cipher = Cipher.getInstance(ALGORITHM_AES_ECB);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedData = cipher.doFinal(decodedData);

            // 4. 返回UTF-8字符串
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new JmComicException("Failed to decrypt API response", e);
        }
    }

    /**
     * 计算字符串的MD5哈希值
     *
     * @param input 输入字符串
     * @return 32位小写的MD5哈希值
     */
    public static String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM_MD5);
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // 转换为16进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // MD5 算法是 Java 标准库保证支持的，理论上不会发生此异常
            throw new JmComicException("MD5 algorithm not found", e);
        }
    }
}
