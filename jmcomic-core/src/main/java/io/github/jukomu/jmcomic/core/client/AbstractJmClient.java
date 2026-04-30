package io.github.jukomu.jmcomic.core.client;

import io.github.jukomu.jmcomic.api.client.*;
import io.github.jukomu.jmcomic.api.enums.ClientType;
import io.github.jukomu.jmcomic.api.exception.NetworkException;
import io.github.jukomu.jmcomic.api.exception.ResponseException;
import io.github.jukomu.jmcomic.api.model.*;
import io.github.jukomu.jmcomic.api.strategy.IAlbumPathGenerator;
import io.github.jukomu.jmcomic.api.strategy.IDownloadPathGenerator;
import io.github.jukomu.jmcomic.api.strategy.IPhotoPathGenerator;
import io.github.jukomu.jmcomic.core.cache.CacheKey;
import io.github.jukomu.jmcomic.core.cache.CachePool;
import io.github.jukomu.jmcomic.core.config.JmConfiguration;
import io.github.jukomu.jmcomic.core.constant.JmConstants;
import io.github.jukomu.jmcomic.core.crypto.JmImageTool;
import io.github.jukomu.jmcomic.core.net.model.JmResponse;
import io.github.jukomu.jmcomic.core.net.provider.DomainProbe;
import io.github.jukomu.jmcomic.core.net.provider.JmDomainManager;
import io.github.jukomu.jmcomic.core.strategy.impl.DefaultAlbumPathGenerator;
import io.github.jukomu.jmcomic.core.strategy.impl.DefaultPhotoPathGenerator;
import io.github.jukomu.jmcomic.core.util.FileUtils;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.CookieManager;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author JUKOMU
 * @Description: JmClient 接口的抽象基类。
 * <p>
 * 封装了线程池、域名管理、图片下载、本子并行下载等通用逻辑。
 * 子类只需要实现 initialize() 和 updateDomains() 以及具体的数据获取方法。
 * @Project: jmcomic-api-java
 * @Date: 2025/10/28
 */
public abstract class AbstractJmClient implements JmClient, JmDownloadClient {
    private final Logger logger = LoggerFactory.getLogger(AbstractJmClient.class);
    protected final JmConfiguration config;
    protected final OkHttpClient httpClient;
    private final ExecutorService internalExecutor;
    private final boolean isExternalExecutor;
    private volatile String loggedInUserName;
    private final CookieManager cookieManager;
    protected final JmDomainManager domainManager;
    protected final CachePool<CacheKey, Object> cachePool;


    protected AbstractJmClient(JmConfiguration config, OkHttpClient httpClient, CookieManager cookieManager, JmDomainManager domainManager) {
        this.config = Objects.requireNonNull(config);
        this.httpClient = Objects.requireNonNull(httpClient);
        this.cookieManager = Objects.requireNonNull(cookieManager);
        this.domainManager = Objects.requireNonNull(domainManager);
        this.domainManager.setInitialized(false);

        /*
         * 线程池优先用用户自定义的，没有就按下载线程池大小配置创建，
         * 未配置时默认取 CPU 核心数。
         */
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
        /*
         * 后台异步初始化：更新域名列表 -> 域名探活排掉死域名 -> 启动定期复探 -> 调子类初始化
         */
        this.internalExecutor.submit(() -> {
            this.updateDomains();
            DomainProbe probe = createDomainProbe();
            this.domainManager.probeAllDomains(probe);
            this.domainManager.startPeriodicProbe(probe, config.getDomainProbeIntervalMs());
            this.domainManager.setInitialized(true);
            this.initialize();
        });
    }

    /**
     * 客户端初始化方法
     */
    protected abstract void initialize();

    /**
     * 更新域名列表
     */
    protected abstract void updateDomains();

    @Override
    public byte[] fetchImageBytes(JmImage image) {
        Request request = new Request.Builder()
                .url(image.getDownloadUrl())
                .get()
                .build();

        /*
         * 图片下载用独立的读超时，避免大图因为全局超时太短下不来。
         */
        OkHttpClient imageClient = httpClient;
        if (!config.getImageTimeout().equals(config.getTimeout())) {
            imageClient = httpClient.newBuilder()
                    .readTimeout(config.getImageTimeout())
                    .build();
        }

        try (Response response = imageClient.newCall(request).execute()) {
            JmResponse jmResponse = new JmResponse(response);
            jmResponse.requireSuccess();
            byte[] content = jmResponse.getContent();
            // 如果是.gif，不进行解密（GIF 图片未经过禁漫加密）
            if (image.isGif()) {
                return content;
            }
            // 对图片进行解密（禁漫图片使用异或/位移等算法加密）
            return JmImageTool.decryptImage(content, image);
        } catch (ResponseException e) {
            throw new ResponseException("Failed to fetch image: " + e.getMessage(), e);
        } catch (IOException e) {
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
        String imageDomain = this.domainManager.getBestDomain();
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

        /*
         * 按域名分批加载新 Cookies：先提取域名去重，再按域名批量写入 CookieJar。
         */
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
        downloadImage(image, Path.of(FileUtils.sanitizeFilename(image.filename())));
    }


    @Override
    public void downloadImage(String imageUrl, Path path) throws IOException {
        JmImage jmImage = new JmImage("", "", "", imageUrl, "", 0);
        downloadImage(jmImage, path);
    }

    @Override
    public void downloadImage(JmImage image, Path path) throws IOException {
        logger.info("开始下载图片: {}", image.getFilename());
        if (Files.isDirectory(path)) {
            // 路径为目录则拼接文件名（净化非法字符）
            path = path.resolve(FileUtils.sanitizeFilename(image.filename()));
        }
        // 对路径的最后一级（文件名）统一净化，防止非法字符写入文件系统
        Path parent = path.getParent();
        String safeFilename = FileUtils.sanitizeFilename(path.getFileName().toString());
        path = parent != null ? parent.resolve(safeFilename) : Path.of(safeFilename);

        // 检查文件是否已存在，避免重复下载
        if (Files.exists(path)) {
            logger.info("图片 {} 已存在，跳过下载", image.getFilename());
            // 尝试清理可能残留的 .tmp 文件（如上次下载在 move 前中断）
            Path staleTmp = path.resolveSibling(path.getFileName() + ".tmp");
            if (Files.exists(staleTmp)) {
                try {
                    Files.delete(staleTmp);
                } catch (IOException ignored) {
                    // 删不掉就算了
                }
            }
            return;
        }
        byte[] imageBytes = fetchImageBytes(image);
        // 确保路径存在
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        /*
         * 先写到 .tmp 再原子重命名，防止下载中断留下残文件。
         * 跨文件系统不支持原子移动时降级为 REPLACE_EXISTING。
         */
        Path tmpPath = path.resolveSibling(path.getFileName() + ".tmp");
        Files.write(tmpPath, imageBytes);
        try {
            Files.move(tmpPath, path, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmpPath, path, StandardCopyOption.REPLACE_EXISTING);
        }
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
        return downloadPhotoInternal(photo, path, executor, null);
    }

    /**
     * 章节图片并行下载的内部实现，支持进度回调。
     *
     * @param photo    章节对象
     * @param path     下载目录
     * @param executor 线程池
     * @param callback 进度回调，可为 null
     */
    private DownloadResult downloadPhotoInternal(JmPhoto photo, Path path, ExecutorService executor,
                                                 Consumer<DownloadProgress> callback) {
        logger.info("开始下载章节: {}", photo.getTitle());
        List<CompletableFuture<Path>> futures = new ArrayList<>();
        ConcurrentHashMap<JmImage, Exception> failedTasks = new ConcurrentHashMap<>();
        AtomicInteger completedImages = new AtomicInteger(0);
        int totalImages = photo.images().size();

        Objects.requireNonNull(path, "Photo path generator returned null for photo " + photo.id());

        // 尝试从缓存获取 albumTitle（downloadPhoto 调用前 album 大概率已被缓存）
        String albumTitle = resolveAlbumTitle(photo.getAlbumId());

        // 一次性提交所有图片任务，由线程池自身控制并发
        for (JmImage image : photo.images()) {
            CompletableFuture<Path> future = CompletableFuture.supplyAsync(() -> {
                try {
                    Path destination = path.resolve(FileUtils.sanitizeFilename(image.filename()));
                    downloadImage(image, destination);
                    if (callback != null) {
                        int completed = completedImages.incrementAndGet();
                        callback.accept(new DownloadProgress(
                                photo.getAlbumId(), albumTitle, photo.id(), photo.getTitle(),
                                completed, totalImages, 0, 0, false
                        ));
                    }
                    return destination;
                } catch (Exception e) {
                    failedTasks.put(image, e);
                    throw new CompletionException(e);
                }
            }, executor);
            futures.add(future);
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            logger.warn("下载章节 '{}' 时部分图片下载失败。", photo.getTitle());
        }

        // 从所有 Future 中筛选出成功完成的任务路径
        List<Path> successfulFiles = Collections.synchronizedList(new ArrayList<>());
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
    public DownloadResult downloadPhoto(JmPhoto photo, IDownloadPathGenerator pathGenerator, ExecutorService executor) {
        logger.info("开始下载章节: {}", photo.getTitle());
        List<CompletableFuture<Path>> futures = new ArrayList<>();
        ConcurrentHashMap<JmImage, Exception> failedTasks = new ConcurrentHashMap<>();

        for (JmImage image : photo.images()) {
            CompletableFuture<Path> future = CompletableFuture.supplyAsync(() -> {
                try {
                    Path destination = pathGenerator.generatePath(null, photo, image);
                    downloadImage(image, destination);
                    return destination;
                } catch (Exception e) {
                    failedTasks.put(image, e);
                    throw new CompletionException(e);
                }
            }, executor);
            futures.add(future);
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            logger.warn("下载章节 '{}' 时部分图片下载失败。", photo.getTitle());
        }

        List<Path> successfulFiles = futures.stream()
                .filter(f -> !f.isCompletedExceptionally())
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        DownloadResult downloadResult = new DownloadResult(successfulFiles, failedTasks);
        logger.info("章节 {} 下载完成. 成功: {}, 失败: {}",
                photo.getTitle(), downloadResult.getSuccessfulFiles().size(), downloadResult.getFailedTasks().size());
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
        return downloadAlbumInternal(album, path, executor, null);
    }

    /**
     * 本子下载的内部实现，支持进度回调。
     * <p>
     * 用 CompletionService 并发拉章节详情，谁先完成就先把它的图片提交到线程池，
     * 边拉边下，最大化并发效率。
     * </p>
     *
     * @param album    本子对象
     * @param path     下载根目录
     * @param executor 线程池
     * @param callback 进度回调，可为 null
     */
    private DownloadResult downloadAlbumInternal(JmAlbum album, Path path, ExecutorService executor,
                                                 Consumer<DownloadProgress> callback) {
        logger.info("开始下载本子: {}", album.getTitle());
        Objects.requireNonNull(path, "Album path generator returned null for album: " + album.id());
        int totalPhotos = album.photoMetas().size();

        // 并发拉取所有章节详情
        List<JmPhotoMeta> photoMetas = album.photoMetas();
        ExecutorCompletionService<JmPhoto> completionService = new ExecutorCompletionService<>(executor);
        ConcurrentHashMap<Future<JmPhoto>, String> futureToPhotoId = new ConcurrentHashMap<>();
        for (JmPhotoMeta photoMeta : photoMetas) {
            String id = photoMeta.id();
            Future<JmPhoto> future = completionService.submit(() -> getPhoto(id));
            futureToPhotoId.put(future, id);
        }

        // 构建 photoMeta 查找表，用于失败日志
        Map<String, JmPhotoMeta> photoMetaMap = new HashMap<>();
        for (JmPhotoMeta meta : photoMetas) {
            photoMetaMap.put(meta.id(), meta);
        }

        // 谁先完成先处理谁 —— 边拉章节边提交图片任务
        ConcurrentHashMap<JmImage, Exception> allFailedTasks = new ConcurrentHashMap<>();
        List<CompletableFuture<Path>> imageFutures = new ArrayList<>();
        AtomicInteger totalImages = new AtomicInteger(0);
        AtomicInteger completedImages = new AtomicInteger(0);
        AtomicInteger completedPhotosCount = new AtomicInteger(0);

        for (int i = 0; i < totalPhotos; i++) {
            // completionService.take() 阻塞等待任意一个章节获取完成
            Future<JmPhoto> future;
            try {
                future = completionService.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            String photoId = futureToPhotoId.get(future);
            JmPhoto fullPhoto;
            try {
                fullPhoto = future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (ExecutionException e) {
                JmPhotoMeta failedMeta = photoMetaMap.get(photoId);
                logger.error("下载章节 '{}' (ID: {}) 失败: {}",
                        failedMeta != null ? failedMeta.getTitle() : "?",
                        photoId,
                        e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                continue;
            }

            // 该章节获取成功，立即提交其图片任务
            // 如果本子只有单个章节，不额外创建子目录
            Path photoPath = fullPhoto.isSingleAlbum()
                    ? path
                    : path.resolve(new DefaultPhotoPathGenerator().generatePath(fullPhoto));
            int photoTotal = fullPhoto.images().size();
            int currentTotal = totalImages.addAndGet(photoTotal);
            AtomicInteger photoCompleted = new AtomicInteger(0);

            for (JmImage image : fullPhoto.images()) {
                CompletableFuture<Path> imgFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        Path destination = photoPath.resolve(FileUtils.sanitizeFilename(image.filename()));
                        downloadImage(image, destination);
                        if (callback != null) {
                            int completed = completedImages.incrementAndGet();
                            int pc = photoCompleted.incrementAndGet();
                            if (pc == photoTotal) {
                                completedPhotosCount.incrementAndGet();
                            }
                            int currentCompletedPhotos = completedPhotosCount.get();
                            callback.accept(new DownloadProgress(
                                    album.id(), album.getTitle(), fullPhoto.id(), fullPhoto.getTitle(),
                                    completed, currentTotal, currentCompletedPhotos, totalPhotos, true
                            ));
                        }
                        return destination;
                    } catch (Exception e) {
                        allFailedTasks.put(image, e);
                        throw new CompletionException(e);
                    }
                }, executor);
                imageFutures.add(imgFuture);
            }
        }

        // 等所有图片下完
        try {
            CompletableFuture.allOf(imageFutures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            logger.warn("下载本子 '{}' 时部分图片下载失败。", album.getTitle());
        }

        List<Path> allSuccessfulFiles = imageFutures.stream()
                .filter(f -> !f.isCompletedExceptionally())
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        DownloadResult downloadResult = new DownloadResult(allSuccessfulFiles, allFailedTasks);
        logger.info("本子 {} 下载完成. 成功图片数: {}, 失败图片数: {}",
                album.getTitle(), downloadResult.getSuccessfulFiles().size(), downloadResult.getFailedTasks().size());
        return downloadResult;
    }

    @Override
    public DownloadResult downloadAlbum(JmAlbum album, IDownloadPathGenerator pathGenerator, ExecutorService executor) {
        logger.info("开始下载本子: {}", album.getTitle());

        // 并发拉取所有章节
        List<JmPhotoMeta> photoMetas = album.photoMetas();
        List<CompletableFuture<JmPhoto>> photoFutures = new ArrayList<>(photoMetas.size());
        for (JmPhotoMeta photoMeta : photoMetas) {
            CompletableFuture<JmPhoto> future = CompletableFuture.supplyAsync(
                    () -> getPhoto(photoMeta.id()), executor);
            photoFutures.add(future);
        }

        // 走完整路径生成器，逐张图片下载
        ConcurrentHashMap<JmImage, Exception> allFailedTasks = new ConcurrentHashMap<>();
        List<CompletableFuture<Path>> imageFutures = new ArrayList<>();

        for (int i = 0; i < photoFutures.size(); i++) {
            JmPhotoMeta photoMeta = photoMetas.get(i);
            CompletableFuture<JmPhoto> future = photoFutures.get(i);
            try {
                JmPhoto fullPhoto = future.join();
                for (JmImage image : fullPhoto.images()) {
                    CompletableFuture<Path> imgFuture = CompletableFuture.supplyAsync(() -> {
                        try {
                            Path destination = pathGenerator.generatePath(album, fullPhoto, image);
                            downloadImage(image, destination);
                            return destination;
                        } catch (Exception e) {
                            allFailedTasks.put(image, e);
                            throw new CompletionException(e);
                        }
                    }, executor);
                    imageFutures.add(imgFuture);
                }
            } catch (CompletionException e) {
                logger.error("下载章节 '{}' (ID: {}) 失败: {}",
                        photoMeta.getTitle(), photoMeta.id(),
                        e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            }
        }

        // 等所有图片下完
        try {
            CompletableFuture.allOf(imageFutures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            logger.warn("下载本子 '{}' 时部分图片下载失败。", album.getTitle());
        }

        List<Path> allSuccessfulFiles = imageFutures.stream()
                .filter(f -> !f.isCompletedExceptionally())
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        DownloadResult downloadResult = new DownloadResult(allSuccessfulFiles, allFailedTasks);
        logger.info("本子 {} 下载完成. 成功图片数: {}, 失败图片数: {}",
                album.getTitle(), downloadResult.getSuccessfulFiles().size(), downloadResult.getFailedTasks().size());
        return downloadResult;
    }

    // == DownloadRequest 入口 ==

    @Override
    public DownloadRequest download(JmAlbum album) {
        return new DownloadRequest(album, req -> {
            Path resolvedPath = req.getPath() != null
                    ? req.getPath()
                    : new DefaultAlbumPathGenerator().generatePath(album);
            ExecutorService exec = req.getExecutorService() != null
                    ? req.getExecutorService()
                    : this.internalExecutor;
            return downloadAlbumInternal(album, resolvedPath, exec, req.getProgressCallback());
        });
    }

    @Override
    public DownloadRequest download(JmPhoto photo) {
        return new DownloadRequest(photo, req -> {
            JmAlbum parentAlbum = getAlbum(photo.getAlbumId());
            Path albumPath = new DefaultAlbumPathGenerator().generatePath(parentAlbum);
            Path photoPath = req.getPath() != null
                    ? req.getPath()
                    : albumPath.resolve(new DefaultPhotoPathGenerator().generatePath(photo));
            ExecutorService exec = req.getExecutorService() != null
                    ? req.getExecutorService()
                    : this.internalExecutor;
            return downloadPhotoInternal(photo, photoPath, exec, req.getProgressCallback());
        });
    }

    // == 辅助方法==

    /**
     * 通用请求执行方法
     *
     * @param request 请求对象
     * @return 通用禁漫响应类
     */
    public JmResponse executeRequest(Request request) throws ResponseException, NetworkException {
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
    protected JmFavoritePage getCachedJmFavoritePage(FavoriteQuery query) {
        int folderId = query.getFolderId();
        int page = query.getPage();
        return (JmFavoritePage) cachePool.get(CacheKey.of(JmFavoritePage.class, folderId + "/" + page));
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
        int folderId = favoritePage.getFolderId();
        int currentPage = favoritePage.getCurrentPage();
        cachePool.put(CacheKey.of(JmFavoritePage.class, folderId + "/" + currentPage), favoritePage);
    }

    /**
     * 从缓存拿本子标题用于进度回调。album 通常已被缓存，不用额外请求网络。
     *
     * @param albumId 本子ID
     * @return 本子标题，没缓存就返回空串
     */
    private String resolveAlbumTitle(String albumId) {
        if (albumId == null || albumId.isEmpty()) {
            return "";
        }
        JmAlbum cached = getCachedJmAlbum(albumId);
        return cached != null ? cached.getTitle() : "";
    }

    // == 资源管理实现 ==

    /**
     * 创建域名探活实现，默认用 HEAD 请求检测根路径可达性。
     * 子类可以覆盖自定义。
     */
    protected DomainProbe createDomainProbe() {
        long timeoutMs = config.getDomainProbeTimeoutMs();
        return domain -> {
            try {
                HttpUrl url = new HttpUrl.Builder()
                        .scheme("https")
                        .host(domain)
                        .build();
                Request request = new Request.Builder()
                        .url(url)
                        .head()
                        .build();
                OkHttpClient probeClient = httpClient.newBuilder()
                        .connectTimeout(java.time.Duration.ofMillis(timeoutMs))
                        .readTimeout(java.time.Duration.ofMillis(timeoutMs))
                        .build();
                try (Response response = probeClient.newCall(request).execute()) {
                    // 响应码 < 500 视为可达（4xx 说明服务器在线，仅权限/路径问题）
                    return response.code() < 500;
                }
            } catch (IOException e) {
                return false;
            }
        };
    }

    @Override
    public void close() {
        // 关闭后台域名复探定时任务
        domainManager.shutdown();

        /*
         * 只关内部创建的线程池，外部传入的由调用方自己管。
         * 超时强制关闭
         */
        if (!isExternalExecutor && internalExecutor != null && !internalExecutor.isShutdown()) {
            internalExecutor.shutdown();
            try {
                long closeTimeoutMs = config.getCloseTimeoutMs();
                if (!internalExecutor.awaitTermination(closeTimeoutMs, TimeUnit.MILLISECONDS)) {
                    logger.warn("线程池未在 {}ms 内完成所有任务，强制关闭", closeTimeoutMs);
                    internalExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                internalExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // 清理 OkHttpClient 的线程池、连接池和缓存
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
        try (var cache = httpClient.cache()) {
            if (cache != null) {
                cache.close();
            }
        } catch (IOException e) {
            logger.warn("关闭缓存时发生 I/O 错误", e);
        }
    }
}
