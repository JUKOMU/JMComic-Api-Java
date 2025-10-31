package io.github.jukomu.jmcomic.core.client;

import io.github.jukomu.jmcomic.api.client.DownloadResult;
import io.github.jukomu.jmcomic.api.client.JmClient;
import io.github.jukomu.jmcomic.api.config.JmConfiguration;
import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.api.exception.ApiResponseException;
import io.github.jukomu.jmcomic.api.exception.NetworkException;
import io.github.jukomu.jmcomic.api.model.JmAlbum;
import io.github.jukomu.jmcomic.api.model.JmImage;
import io.github.jukomu.jmcomic.api.model.JmPhoto;
import io.github.jukomu.jmcomic.api.model.JmPhotoMeta;
import io.github.jukomu.jmcomic.api.strategy.AlbumPathGenerator;
import io.github.jukomu.jmcomic.api.strategy.PhotoPathGenerator;
import io.github.jukomu.jmcomic.core.constant.JmConstants;
import io.github.jukomu.jmcomic.core.net.model.JmResponse;
import io.github.jukomu.jmcomic.core.net.provider.JmDomainManager;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.CookieManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author JUKOMU
 * @Description: JmClient 接口的抽象基类
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public abstract class AbstractJmClient implements JmClient {

    protected final JmConfiguration config;
    protected final OkHttpClient httpClient;
    private final ExecutorService internalExecutor;
    private final boolean isExternalExecutor;
    private volatile String loggedInUserName;
    private final CookieManager cookieManager;
    protected final JmDomainManager domainManager;

    protected AbstractJmClient(JmConfiguration config, OkHttpClient httpClient, CookieManager cookieManager, JmDomainManager domainManager) {
        this.config = Objects.requireNonNull(config);
        this.httpClient = Objects.requireNonNull(httpClient);
        this.cookieManager = Objects.requireNonNull(cookieManager);
        this.domainManager = Objects.requireNonNull(domainManager);

        // 根据配置决定 ExecutorService
        if (config.getDownloadExecutor() != null) {
            this.internalExecutor = config.getDownloadExecutor();
            this.isExternalExecutor = true;
        } else {
            int poolSize = (config.getDownloadThreadPoolSize() > 0)
                    ? config.getDownloadThreadPoolSize()
                    : Runtime.getRuntime().availableProcessors();
            this.internalExecutor = Executors.newFixedThreadPool(poolSize);
            this.isExternalExecutor = false;
        }
        this.initialize();
    }

    /**
     * 客户端初始化方法
     */
    protected abstract void initialize();

    protected abstract void updateDomains();

    /**
     * 获取客户端的类型
     *
     * @return 客户端的类型
     */
    public ClientType getClientType() {
        return config.getClientType();
    }

    // == 会话管理层 (公共实现) ==

    @Override
    public List<Cookie> getCookies() {
        CookieJar cookieJar = httpClient.cookieJar();
        HttpUrl dummyUrl = newHttpUrlBuilder().build();
        return cookieJar.loadForRequest(dummyUrl);
    }

    @Override
    public void setCookies(List<Cookie> cookies) {
        // 清除所有 cookies
        this.cookieManager.getCookieStore().removeAll();

        // 加载新 Cookies
        if (cookies != null && !cookies.isEmpty()) {
            CookieJar cookieJar = httpClient.cookieJar();
            cookies.stream()
                    .map(Cookie::domain)
                    .distinct()
                    .forEach(domain -> {
                        HttpUrl urlForDomain = new HttpUrl.Builder().scheme("https").host(domain).build();
                        List<Cookie> cookiesForDomain = cookies.stream()
                                .filter(c -> c.domain().equals(domain))
                                .collect(Collectors.toList());
                        cookieJar.saveFromResponse(urlForDomain, cookiesForDomain);
                    });
        }
    }

    // == 便利操作层实现 ==

    @Override
    public void downloadImage(JmImage image, Path destinationPath) throws IOException {
        byte[] imageBytes = fetchImageBytes(image);
        // Ensure parent directory exists
        if (destinationPath.getParent() != null) {
            Files.createDirectories(destinationPath.getParent());
        }
        Files.write(destinationPath, imageBytes);
    }

    @Override
    public DownloadResult downloadPhoto(JmPhoto photo, PhotoPathGenerator pathGenerator) {
        return downloadPhoto(photo, pathGenerator, this.internalExecutor);
    }

    @Override
    public DownloadResult downloadPhoto(JmPhoto photo, PhotoPathGenerator pathGenerator, ExecutorService executor) {
        List<CompletableFuture<Path>> futures = new ArrayList<>();
        List<Path> successfulFiles = Collections.synchronizedList(new ArrayList<>());
        ConcurrentHashMap<JmImage, Exception> failedTasks = new ConcurrentHashMap<>();

        for (JmImage image : photo.images()) {
            CompletableFuture<Path> future = CompletableFuture.supplyAsync(() -> {
                try {
                    // The pathGenerator now provides the directory for the photo.
                    Path photoDir = pathGenerator.generatePath(photo);
                    Objects.requireNonNull(photoDir, "Photo path generator returned null for photo " + photo.id());

                    // Resolve the final image file path within the photo directory.
                    Path destination = photoDir.resolve(image.filename());

                    downloadImage(image, destination);
                    return destination;
                } catch (Exception e) {
                    failedTasks.put(image, e);
                    throw new CompletionException(e); // Wrap exception for later processing
                }
            }, executor);
            futures.add(future);
        }

        // Wait for all tasks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Collect successful results
        for (CompletableFuture<Path> future : futures) {
            if (!future.isCompletedExceptionally()) {
                successfulFiles.add(future.join());
            }
        }

        return new DownloadResult(successfulFiles, failedTasks);
    }

    @Override
    public DownloadResult downloadAlbum(JmAlbum album, AlbumPathGenerator pathGenerator) {
        return downloadAlbum(album, pathGenerator, this.internalExecutor);
    }

    @Override
    public DownloadResult downloadAlbum(JmAlbum album, AlbumPathGenerator pathGenerator, ExecutorService executor) {
        List<CompletableFuture<DownloadResult>> photoFutures = new ArrayList<>();

        // 1. Use the AlbumPathGenerator to get the root directory for this album.
        final Path albumPath = pathGenerator.generatePath(album);
        Objects.requireNonNull(albumPath, "Album path generator returned null for album: " + album.id());

        // Create a download task for each chapter (photo)
        for (JmPhotoMeta photoMeta : album.photoMetas()) {
            CompletableFuture<DownloadResult> future = CompletableFuture.supplyAsync(() -> {
                // Get the full Photo details within the async task
                JmPhoto fullPhoto = getPhoto(photoMeta.id());

                // 2. This generator creates a directory for the photo inside the album directory.
                PhotoPathGenerator photoDirGenerator = photo -> {
                    String photoDirName = photo.id();
                    if (StringUtils.isBlank(photoDirName)) {
                        // Fallback using sort order if photo ID is not available
                        photoDirName = "chapter-" + photo.sortOrder();
                    }
                    return albumPath.resolve(photoDirName);
                };

                // 3. Download this Photo, which will download all its images.
                return downloadPhoto(fullPhoto, photoDirGenerator, executor);
            }, executor);
            photoFutures.add(future);
        }

        // Wait for all chapter download tasks to complete
        CompletableFuture.allOf(photoFutures.toArray(new CompletableFuture[0])).join();

        // Merge the download results from all chapters
        List<Path> allSuccessfulFiles = Collections.synchronizedList(new ArrayList<>());
        ConcurrentHashMap<JmImage, Exception> allFailedTasks = new ConcurrentHashMap<>();

        for (CompletableFuture<DownloadResult> future : photoFutures) {
            if (!future.isCompletedExceptionally()) {
                DownloadResult result = future.join();
                allSuccessfulFiles.addAll(result.getSuccessfulFiles());
                allFailedTasks.putAll(result.getFailedTasks());
            }
        }

        return new DownloadResult(allSuccessfulFiles, allFailedTasks);
    }


    // == 辅助方法==

    /**
     * 通用请求执行方法
     *
     * @param request 请求对象
     * @return 通用禁漫响应类
     */
    public JmResponse executeRequest(Request request) throws ApiResponseException, NetworkException {
        try (Response response = httpClient.newCall(request).execute()) {
            JmResponse jmResponse = new JmResponse(response);
            jmResponse.requireSuccess();
            return jmResponse;
        } catch (IOException e) {
            throw new NetworkException("Request failed due to I/O error", e);
        }
    }

    /**
     * 缓存用户名
     *
     * @param username 用户名
     */
    protected void cacheUsername(String username) {
        this.loggedInUserName = username;
    }

    /**
     * 获取登录用户名
     *
     * @return 当前登录的用户名
     */
    protected String getLoggedInUserName() {
        String username = this.loggedInUserName;
        if (StringUtils.isBlank(username)) {
            throw new IllegalStateException("Username is required for this operation. Please login first.");
        }
        return username;
    }

    /**
     * 构建基础URL构建器
     *
     * @return HttpUrl Builder
     */
    protected HttpUrl.Builder newHttpUrlBuilder() {
        // 这个方法很重要，它确保了所有请求都指向一个有效的、由DomainManager管理的域名
        // 我们只需要提供一个占位符域名，它将被拦截器替换
        return new HttpUrl.Builder()
                .scheme("https")
                .host(JmConstants.PLACEHOLDER_HOST);
    }

    protected Request.Builder getGetRequestBuilder(HttpUrl url) {
        return new Request.Builder().url(url).get();
    }

    protected Request.Builder getPostRequestBuilder(HttpUrl url, RequestBody requestBody) {
        return new Request.Builder().url(url).post(requestBody);
    }

    // == 资源管理实现 ==

    @Override
    public void close() {
        // 只有当 ExecutorService 是由本客户端内部创建时，才负责关闭它
        if (!isExternalExecutor && internalExecutor != null && !internalExecutor.isShutdown()) {
            internalExecutor.shutdown();
        }
        // OkHttpClient 内部有自己的连接池和线程池，也需要关闭
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
        try (var cache = httpClient.cache()) {
            if (cache != null) {
                cache.close();
            }
        } catch (IOException e) {
            // Log the exception, but don't rethrow from close()
        }
    }
}
