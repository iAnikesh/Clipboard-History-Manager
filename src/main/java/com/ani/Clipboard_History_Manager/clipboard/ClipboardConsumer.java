package com.ani.Clipboard_History_Manager.clipboard;

import com.ani.Clipboard_History_Manager.cache.LRUCache;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ClipboardConsumer {

    private final ClipboardWatcher watcher;
    private final ClipboardItemRepository repository;
    private final LRUCache<String, ClipboardItem> cache;
    private final ExecutorService executor;
    private volatile boolean running = true;

    public ClipboardConsumer(ClipboardWatcher watcher, ClipboardItemRepository repository) {
        this.watcher = watcher;
        this.repository = repository;
        this.cache = new LRUCache<>(50); // Default capacity 50 items
        this.executor = Executors.newSingleThreadExecutor(Thread.ofVirtual().factory());
    }

    public interface CacheUpdateListener {
        void onCacheUpdated();
    }
    private final List<CacheUpdateListener> listeners = new java.util.concurrent.CopyOnWriteArrayList<>();

    public void addListener(CacheUpdateListener listener) {
        listeners.add(listener);
    }

    @PostConstruct
    public void startConsuming() {
        // Load history into cache on startup
        List<ClipboardItem> history = repository.findAll(Sort.by(Sort.Direction.ASC, "timestamp"));
        for (ClipboardItem item : history) {
            cache.put(item.getHash(), item);
        }
        System.out.println("Loaded " + history.size() + " items from database into cache.");
        notifyListeners();

        executor.submit(() -> {
            System.out.println("Clipboard consumer started on virtual thread: " + Thread.currentThread());
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    ClipboardItem item = watcher.getQueue().take();
                    String hash = computeHash(digest, item.getContent());
                    item.setHash(hash);
                    
                    // The LRU cache automatically dedupes:
                    // If the hash already exists, it updates the value and moves it to the head.
                    cache.put(hash, item);
                    
                    // Persistence layer — write through to DB
                    ClipboardItem existing = repository.findByHash(hash);
                    if (existing != null) {
                        existing.setTimestamp(item.getTimestamp());
                        repository.save(existing);
                    } else {
                        repository.save(item);
                    }
                    
                    System.out.println("Consumer processed item. Cache size: " + cache.size() + ", Hash: " + hash);
                    notifyListeners();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    private void notifyListeners() {
        listeners.forEach(CacheUpdateListener::onCacheUpdated);
    }

    @PreDestroy
    public void stopConsuming() {
        running = false;
        executor.shutdownNow();
    }
    
    public LRUCache<String, ClipboardItem> getCache() {
        return cache;
    }

    private String computeHash(MessageDigest digest, String content) {
        byte[] hashBytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }
}
