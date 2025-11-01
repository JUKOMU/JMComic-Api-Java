package io.github.jukomu.jmcomic.core.cache;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author JUKOMU
 * @Description: LFU缓存池
 * 当缓存满时，会淘汰使用频率最低且最早放入的条目
 * @Project: jmcomic-api-java
 * @Date: 2025/11/1
 */
public final class CachePool<K, V> {

    private static class Node<K, V> {
        final K key;
        V value;
        int freq = 1;
        int weight;

        Node(K key, V value, int weight) {
            this.key = key;
            this.value = value;
            this.weight = weight;
        }
    }

    // 单位: Byte
    private final long capacity;
    // 单位: Byte
    private long currentSize;
    private int minFreq;
    private final Map<K, Node<K, V>> cacheMap;
    private final Map<Integer, LinkedHashSet<Node<K, V>>> freqMap;
    private final CacheObjectSizer sizer;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public CachePool(long capacityInBytes) {
        this.capacity = capacityInBytes;
        this.sizer = new CacheObjectSizer();
        this.currentSize = 0;
        this.minFreq = 0;
        this.cacheMap = new HashMap<>();
        this.freqMap = new HashMap<>();
    }

    /**
     * 从缓存中获取值。如果命中，会增加其使用频率。
     *
     * @param key 缓存键
     * @return 如果存在则返回值，否则返回 null。
     */
    public V get(K key) {
        readLock.lock();
        try {
            Node<K, V> node = cacheMap.get(key);
            if (node == null) {
                return null;
            }
            // 缓存命中，需要升级锁来更新频率
            readLock.unlock();
            writeLock.lock();
            try {
                // 双重检查
                node = cacheMap.get(key);
                if (node != null) {
                    updateFreq(node);
                    return node.value;
                }
                return null; // 在锁升级期间被移除了
            } finally {
                readLock.lock(); // 锁降级
                writeLock.unlock();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 将一个键值对放入缓存。如果键已存在，则更新其值。
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    public void put(K key, V value) {
        if (capacity <= 0 || value == null) {
            return;
        }
        writeLock.lock();
        try {
            Node<K, V> node = cacheMap.get(key);
            if (node != null) {
                // 更新已存在的值
                currentSize -= node.weight;
                node.value = value;
                node.weight = sizer.sizeOf(value);
                currentSize += node.weight;
                updateFreq(node);
            } else {
                // 插入新值
                int weight = sizer.sizeOf(value);
                // 如果单个对象就超过容量，则不缓存
                if (weight > capacity) {
                    return;
                }
                // 淘汰直到有足够空间
                while (currentSize + weight > capacity) {
                    evict();
                }

                Node<K, V> newNode = new Node<>(key, value, weight);
                addNode(newNode);
                currentSize += weight;
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 从缓存中移除指定的键。
     *
     * @param key 要移除的键
     */
    public void remove(K key) {
        writeLock.lock();
        try {
            Node<K, V> node = cacheMap.remove(key);
            if (node != null) {
                LinkedHashSet<Node<K, V>> set = freqMap.get(node.freq);
                if (set != null) {
                    set.remove(node);
                }
                currentSize -= node.weight;
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 清空整个缓存。
     */
    public void clear() {
        writeLock.lock();
        try {
            cacheMap.clear();
            freqMap.clear();
            currentSize = 0;
            minFreq = 0;
        } finally {
            writeLock.unlock();
        }
    }

    private void addNode(Node<K, V> node) {
        cacheMap.put(node.key, node);
        freqMap.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(node);
        minFreq = 1;
    }

    private void updateFreq(Node<K, V> node) {
        int oldFreq = node.freq;
        LinkedHashSet<Node<K, V>> oldSet = freqMap.get(oldFreq);
        if (oldSet != null) {
            oldSet.remove(node);
            if (oldSet.isEmpty() && oldFreq == minFreq) {
                minFreq++;
            }
        }

        node.freq++;
        freqMap.computeIfAbsent(node.freq, k -> new LinkedHashSet<>()).add(node);
    }

    private void evict() {
        LinkedHashSet<Node<K, V>> minFreqSet = freqMap.get(minFreq);
        if (minFreqSet == null || minFreqSet.isEmpty()) {
            // 如果最低频率集合为空，尝试增加minFreq寻找下一个可淘汰的集合
            // 这种情况可能在updateFreq后发生
            minFreq++;
            minFreqSet = freqMap.get(minFreq);
            if (minFreqSet == null || minFreqSet.isEmpty()) {
                return; // 缓存为空，不应发生
            }
        }

        // 淘汰集合中第一个（即最早插入）的节点
        Node<K, V> nodeToEvict = minFreqSet.iterator().next();
        minFreqSet.remove(nodeToEvict);
        cacheMap.remove(nodeToEvict.key);
        currentSize -= nodeToEvict.weight;
    }
}
