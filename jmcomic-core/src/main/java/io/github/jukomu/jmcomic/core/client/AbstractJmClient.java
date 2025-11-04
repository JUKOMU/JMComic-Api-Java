package io.github.jukomu.jmcomic.core.client;

import io.github.jukomu.jmcomic.api.client.DownloadResult;
import io.github.jukomu.jmcomic.api.client.JmClient;
import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.api.exception.ApiResponseException;
import io.github.jukomu.jmcomic.api.exception.NetworkException;
import io.github.jukomu.jmcomic.api.model.*;
import io.github.jukomu.jmcomic.api.strategy.IAlbumPathGenerator;
import io.github.jukomu.jmcomic.api.strategy.IImagePathGenerator;
import io.github.jukomu.jmcomic.api.strategy.IPhotoPathGenerator;
import io.github.jukomu.jmcomic.core.cache.CacheKey;
import io.github.jukomu.jmcomic.core.cache.CachePool;
import io.github.jukomu.jmcomic.core.config.JmConfiguration;
import io.github.jukomu.jmcomic.core.constant.JmConstants;
import io.github.jukomu.jmcomic.core.crypto.JmImageTool;
import io.github.jukomu.jmcomic.core.net.model.JmResponse;
import io.github.jukomu.jmcomic.core.net.provider.JmDomainManager;
import io.github.jukomu.jmcomic.core.strategy.impl.DefaultAlbumPathGenerator;
import io.github.jukomu.jmcomic.core.strategy.impl.DefaultImagePathGenerator;
import io.github.jukomu.jmcomic.core.strategy.impl.DefaultPhotoPathGenerator;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.CookieManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author JUKOMU
 * @Description: JmClient 接口的抽象基类
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public abstract class AbstractJmClient implements JmClient {
    private final Logger logger = LoggerFactory.getLogger(AbstractJmClient.class);
    protected final JmConfiguration config;
    protected final OkHttpClient httpClient;
    private final ExecutorService internalExecutor;
    private final boolean isExternalExecutor;
    private volatile String loggedInUserName;
    private final CookieManager cookieManager;
    protected final JmDomainManager domainManager;
    protected final CachePool<CacheKey, Object> cachePool;
    protected final int concurrentPhotoDownloads;
    protected final int concurrentImageDownloads;

    protected AbstractJmClient(JmConfiguration config, OkHttpClient httpClient, CookieManager cookieManager, JmDomainManager domainManager) {
        this.config = Objects.requireNonNull(config);
        this.httpClient = Objects.requireNonNull(httpClient);
        this.cookieManager = Objects.requireNonNull(cookieManager);
        this.domainManager = Objects.requireNonNull(domainManager);

        // 根据配置决定 ExecutorService
        if (config.getExecutor() != null) {
            this.internalExecutor = config.getExecutor();
            this.isExternalExecutor = true;
        } else {
            int poolSize = (config.getDownloadThreadPoolSize() > 0)
                    ? config.getDownloadThreadPoolSize()
                    : Runtime.getRuntime().availableProcessors();
            this.internalExecutor = Executors.newFixedThreadPool(poolSize);
            this.isExternalExecutor = false;
        }
        // 根据配置决定 CachePool
        this.cachePool = config.getCachePool();
        // 同时下载的章节数
        this.concurrentPhotoDownloads = config.getConcurrentPhotoDownloads();
        // 同时下载的图片数
        this.concurrentImageDownloads = config.getConcurrentImageDownloads();
        this.initialize();
    }

    /**
     * 客户端初始化方法
     */
    protected abstract void initialize();

    protected abstract void updateDomains();

    @Override
    public byte[] fetchImageBytes(JmImage image) {
        Request request = new Request.Builder()
                .url(image.getDownloadUrl())
                .get()
                .build();

        try {
            JmResponse jmResponse = executeRequest(request);
            // 如果是.gif，不进行解密
            if (image.isGif()) {
                return jmResponse.getContent();
            }
            // 对图片进行解密
            return JmImageTool.decryptImage(jmResponse.getContent(), image);
        } catch (ApiResponseException e) {
            throw new ApiResponseException("Failed to fetch image: " + e.getMessage());
        } catch (NetworkException e) {
            throw new NetworkException("Failed to fetch image due to I/O error", e);
        }
    }

    /**
     * 根据本子id生成封面url
     *
     * @param albumId 本子id
     * @param size    尺寸后缀，详情页无，搜索页为 “_3x4”
     * @return 封面url
     */
    public String getAlbumCoverUrl(String albumId, String size) {
        String imageDomain = JmConstants.DEFAULT_IMAGE_DOMAINS.get(new Random().nextInt(JmConstants.DEFAULT_IMAGE_DOMAINS.size()));
        return getAlbumCoverUrl(albumId, imageDomain, size);
    }

    /**
     * 根据本子id生成封面url
     *
     * @param albumId     本子id
     * @param imageDomain 图片cdn域名
     * @param size        尺寸后缀，详情页无，搜索页为 “_3x4”
     * @return 封面url
     */
    public String getAlbumCoverUrl(String albumId, String imageDomain, String size) {
        String path = "/media/albums/" + albumId + size + ".jpg";
        if (imageDomain.startsWith(JmConstants.PROTOCOL_HTTPS)) {
            return imageDomain + path;
        }
        return JmConstants.PROTOCOL_HTTPS + imageDomain + path;
    }

    /**
     * 获取客户端的类型
     *
     * @return 客户端的类型
     */
    public ClientType getClientType() {
        return config.getClientType();
    }

    // == 会话管理层 (公共实现) ==

    public List<Cookie> getCookies() {
        CookieJar cookieJar = httpClient.cookieJar();
        HttpUrl dummyUrl = newHttpUrlBuilder().build();
        return cookieJar.loadForRequest(dummyUrl);
    }

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
    public void downloadImage(JmImage image) throws IOException {
        downloadImage(image, new DefaultImagePathGenerator().generatePath(image));
    }


    @Override
    public void downloadImage(String imageUrl, Path path) throws IOException {
        JmImage jmImage = new JmImage("", "", "", imageUrl, "", 0);
        downloadImage(jmImage, path);
    }

    @Override
    public void downloadImage(JmImage image, IImagePathGenerator imagePathGenerator) throws IOException {
        downloadImage(image, imagePathGenerator.generatePath(image));
    }

    @Override
    public void downloadImage(JmImage image, Path path) throws IOException {
        logger.info("开始下载图片: {}", image.getFilename());
        if (Files.isDirectory(path)) {
            // 路径为目录则拼接文件名
            path = path.resolve(image.getFilename());
        }
        // 检查文件是否存在
        if (Files.exists(path)) {
            logger.info("图片 {} 已存在，跳过下载", image.getFilename());
            return;
        }
        byte[] imageBytes = fetchImageBytes(image);
        // 确保路径存在
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.write(path, imageBytes);
        logger.info("图片 {} 下载完成", image.getFilename());
    }

    @Override
    public DownloadResult downloadPhoto(JmPhoto photo) {
        return downloadPhoto(photo, new DefaultPhotoPathGenerator());
    }

    @Override
    public DownloadResult downloadPhoto(JmPhoto photo, IPhotoPathGenerator pathGenerator) {
        JmAlbum album = getAlbum(photo.getAlbumId());
        // 拼接完整路径
        Path pathAlbum = new DefaultAlbumPathGenerator().generatePath(album);
        Path pathPhoto = pathGenerator.generatePath(photo);
        return downloadPhoto(photo, pathAlbum.resolve(pathPhoto));
    }

    @Override
    public DownloadResult downloadPhoto(JmPhoto photo, Path path) {
        return downloadPhoto(photo, path, this.internalExecutor);
    }

    @Override
    public DownloadResult downloadPhoto(JmPhoto photo, IPhotoPathGenerator pathGenerator, ExecutorService executor) {
        JmAlbum album = getAlbum(photo.getAlbumId());
        // 拼接完整路径
        Path pathAlbum = new DefaultAlbumPathGenerator().generatePath(album);
        Path pathPhoto = pathGenerator.generatePath(photo);
        return downloadPhoto(photo, pathAlbum.resolve(pathPhoto), executor);
    }

    @Override
    public DownloadResult downloadPhoto(JmPhoto photo, Path path, ExecutorService executor) {
        logger.info("开始下载章节: {}", photo.getTitle());
        Semaphore semaphore = new Semaphore(concurrentImageDownloads);
        List<CompletableFuture<Path>> futures = new ArrayList<>();
        List<Path> successfulFiles = Collections.synchronizedList(new ArrayList<>());
        ConcurrentHashMap<JmImage, Exception> failedTasks = new ConcurrentHashMap<>();

        for (JmImage image : photo.images()) {
            try {
                semaphore.acquire();
                CompletableFuture<Path> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        Objects.requireNonNull(path, "Photo path generator returned null for photo " + photo.id());

                        Path destination = path.resolve(image.filename());

                        downloadImage(image, destination);
                        return destination;
                    } catch (Exception e) {
                        failedTasks.put(image, e);
                        throw new CompletionException(e);
                    }
                }, executor);
                future.whenComplete((result, throwable) -> semaphore.release());
                futures.add(future);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                failedTasks.put(image, e);
                break;
            }
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            logger.warn("下载章节 '{}' 时部分图片下载失败，但这不会中断整体下载流程。", photo.getTitle());
        }

        for (CompletableFuture<Path> future : futures) {
            if (!future.isCompletedExceptionally()) {
                successfulFiles.add(future.join());
            }
        }

        DownloadResult downloadResult = new DownloadResult(successfulFiles, failedTasks);
        logger.info("章节 {} 下载完成. 成功: {}, 失败: {}", photo.getTitle(), downloadResult.getSuccessfulFiles().size(), downloadResult.getFailedTasks().size());
        return downloadResult;
    }

    @Override
    public DownloadResult downloadAlbum(JmAlbum album) {
        return downloadAlbum(album, new DefaultAlbumPathGenerator());
    }

    @Override
    public DownloadResult downloadAlbum(JmAlbum album, IAlbumPathGenerator pathGenerator) {
        return downloadAlbum(album, pathGenerator, this.internalExecutor);
    }

    @Override
    public DownloadResult downloadAlbum(JmAlbum album, Path path) {
        return downloadAlbum(album, path, this.internalExecutor);
    }

    @Override
    public DownloadResult downloadAlbum(JmAlbum album, IAlbumPathGenerator pathGenerator, ExecutorService executor) {
        return downloadAlbum(album, pathGenerator.generatePath(album), executor);
    }

    @Override
    public DownloadResult downloadAlbum(JmAlbum album, Path path, ExecutorService executor) {
        logger.info("开始下载本子: {}", album.getTitle());
        Semaphore semaphore = new Semaphore(concurrentPhotoDownloads);
        // 有具体路径时直接下载章节直接无需拼接album路径
        List<CompletableFuture<DownloadResult>> photoFutures = new ArrayList<>();
        Objects.requireNonNull(path, "Album path generator returned null for album: " + album.id());

        for (JmPhotoMeta photoMeta : album.photoMetas()) {
            try {
                semaphore.acquire();
                CompletableFuture<DownloadResult> future = CompletableFuture.supplyAsync(() -> {
                    JmPhoto fullPhoto = getPhoto(photoMeta.id());
                    return downloadPhoto(fullPhoto, path.resolve(new DefaultPhotoPathGenerator().generatePath(fullPhoto)), executor);
                }, executor);
                future.whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        logger.error("下载章节 '{}' (ID: {}) 失败: {}", photoMeta.getTitle(), photoMeta.id(), throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage());
                    }
                    semaphore.release();
                });
                photoFutures.add(future);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("下载本子 '{}' 的过程被中断", album.getTitle());
                break;
            }

        }

        try {
            CompletableFuture.allOf(photoFutures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            logger.warn("下载本子 '{}' 时部分章节下载失败，但这不会中断整体下载流程。", album.getTitle());
        }
        List<Path> allSuccessfulFiles = Collections.synchronizedList(new ArrayList<>());
        ConcurrentHashMap<JmImage, Exception> allFailedTasks = new ConcurrentHashMap<>();

        for (CompletableFuture<DownloadResult> future : photoFutures) {
            if (!future.isCompletedExceptionally()) {
                DownloadResult result = future.join();
                allSuccessfulFiles.addAll(result.getSuccessfulFiles());
                allFailedTasks.putAll(result.getFailedTasks());
            }
        }

        DownloadResult downloadResult = new DownloadResult(allSuccessfulFiles, allFailedTasks);
        logger.info("本子 {} 下载完成. 成功图片数: {}, 失败图片数: {}", album.getTitle(), downloadResult.getSuccessfulFiles().size(), downloadResult.getFailedTasks().size());
        return downloadResult;
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

    // == 缓存辅助方法 ==

    /**
     * 获取本子缓存
     *
     * @param albumId 本子id
     * @return 本子详情
     */
    protected JmAlbum getCachedJmAlbum(String albumId) {
        return (JmAlbum) cachePool.get(CacheKey.of(JmAlbum.class, albumId));
    }

    /**
     * 获取章节缓存
     *
     * @param photoId 章节id
     * @return 章节详情
     */
    protected JmPhoto getCachedJmPhoto(String photoId) {
        return (JmPhoto) cachePool.get(CacheKey.of(JmPhoto.class, photoId));
    }

    /**
     * 获取收藏夹缓存
     *
     * @return 收藏夹详情
     */
    protected JmFavoritePage getCachedJmFavoritePage(int page) {
        return (JmFavoritePage) cachePool.get(CacheKey.of(JmFavoritePage.class, String.valueOf(page)));
    }

    /**
     * 缓存本子详情
     *
     * @param album 本子详情
     */
    protected void cacheJmAlbum(JmAlbum album) {
        cachePool.put(CacheKey.of(JmAlbum.class, album.id()), album);
    }

    /**
     * 缓存章节详情
     *
     * @param photo 章节详情
     */
    protected void cacheJmPhoto(JmPhoto photo) {
        cachePool.put(CacheKey.of(JmPhoto.class, photo.id()), photo);
    }

    /**
     * 缓存用户收藏夹详情
     *
     * @param favoritePage 收藏夹详情
     */
    protected void cacheJmFavoritePage(JmFavoritePage favoritePage) {
        cachePool.put(CacheKey.of(JmFavoritePage.class, String.valueOf(favoritePage.getCurrentPage())), favoritePage);
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
