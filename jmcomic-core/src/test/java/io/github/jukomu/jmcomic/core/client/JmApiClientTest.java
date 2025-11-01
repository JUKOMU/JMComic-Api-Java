package io.github.jukomu.jmcomic.core.client;

import io.github.jukomu.jmcomic.api.client.JmClient;
import io.github.jukomu.jmcomic.api.config.JmConfiguration;
import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.api.model.*;
import io.github.jukomu.jmcomic.api.strategy.AlbumPathGenerator;
import io.github.jukomu.jmcomic.api.strategy.PhotoPathGenerator;
import io.github.jukomu.jmcomic.core.JmComic;
import io.github.jukomu.jmcomic.core.util.FileUtils;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author JUKOMU
 * @Description:
 * @Project: jmcomic-api-java
 * @Date: 2025/10/31
 */
public class JmApiClientTest {
    private final String albumId1 = "58410";
    private final String albumId2 = "60600";
    private final String albumId3 = "77613";
    private final String albumId4 = "86300";
    private final String albumId5 = "540709";

    private final String photoId1 = "1064000";
    private final String photoId2 = "1064001";
    private final String photoId3 = "1064002";
    private final String photoId4 = "1064003";
    private final String photoId5 = "1064004";

    private final String missingAlbumId = "999999999999";
    private final String missingPhotoId = "999999999999";

    private final String albumForImages = "384743";

    static JmConfiguration.Builder jmConfigurationBuilder = new JmConfiguration.Builder()
            .clientType(ClientType.API);
    static JmClient jmClient = JmComic.newClient(jmConfigurationBuilder.build());
//    static {
//        jmClient.login("xxx", "xxx");
//    }

//    @Test
//    @Order(1)
    public void testLogin() {
        JmUserInfo userInfo = jmClient.login("xxx", "xxx");
        assertNotNull(userInfo, "登录用户不应为空");
        System.out.println("✅ [SUCCESS] 登录 " + userInfo.username() + " 获取成功。");
    }

    @ParameterizedTest
    @ValueSource(strings = {albumId1, albumId2, albumId3, albumId4, albumId5})
    @Order(2)
    public void testGetAlbum(String albumId) {
        assertNotNull(jmClient.getAlbum(albumId), "本子" + albumId1 + "结果不应为null");
        System.out.println("✅ [SUCCESS] 本子 " + albumId + " 获取成功。");
    }

    @ParameterizedTest
    @ValueSource(strings = {photoId1, photoId2, photoId3, photoId4, photoId5})
    @Order(3)
    public void testGetPhoto(String photoId) {
        assertNotNull(jmClient.getPhoto(photoId), "章节" + photoId1 + "结果不应为null");
        System.out.println("✅ [SUCCESS] 章节 " + photoId + " 获取成功。");
    }

    @TestFactory
    @Order(4)
    Stream<DynamicTest> testAllImagesDynamically() {
        System.out.println("开始为本子 " + albumForImages + " 生成图片测试...");
        JmAlbum album = jmClient.getAlbum(albumForImages);
        JmPhotoMeta photoMeta = album.getPhotoMeta(1);
        JmPhoto photo = jmClient.getPhoto(photoMeta.id());
        List<JmImage> images = photo.images();

        return images.stream().map(img ->
                DynamicTest.dynamicTest(
                        "测试图片: " + img.getTag(),
                        () -> {
                            byte[] imageBytes = jmClient.fetchImageBytes(img);
                            assertNotNull(imageBytes, "图片 " + img.getTag() + " 的字节结果不应为null");
                            System.out.println("✅ [SUCCESS] 图片 " + img.getTag() + " 获取成功。");
                        }
                )
        );
    }

    @Test
    public void testDownloadAlbum() {
        JmAlbum album = jmClient.getAlbum(albumId5);
        System.out.println(album);
    }

//    @Test
    public void testDownloadPhoto() {
        JmPhoto photo = jmClient.getPhoto(photoId1);
        PhotoPathGenerator photoPathGenerator = photo1 -> Path.of(photo.albumId(), FileUtils.sanitizeFilename(photo.title()) , photo.id());
        jmClient.downloadPhoto(photo, photoPathGenerator);
    }
}
