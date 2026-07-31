package com.ani.Clipboard_History_Manager.clipboard;

import com.ani.Clipboard_History_Manager.cache.LRUCache;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ClipboardConsumer {

    private final ClipboardWatcher watcher;
    private final LRUCache<String, ClipboardItem> cache;
    private final ExecutorService executor;
    private volatile boolean running = true;

    public ClipboardConsumer(ClipboardWatcher watcher) {
        this.watcher = watcher;
        this.cache = new LRUCache<>(50); // Default capacity 50 items
        // Use a single virtual thread per task, or an executor
        // Since it's a consumer, we can just use one virtual thread
        this.executor = Executors.newSingleThreadExecutor(Thread.ofVirtual().factory());
    }

    @PostConstruct
    public void startConsuming() {
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
                    String hash = computeHash(digest, item.content());
                    
                    // The LRU cache automatically dedupes:
                    // If the hash already exists, it updates the value and moves it to the head.
                    cache.put(hash, item);
                    
                    System.out.println("Consumer processed item. Cache size: " + cache.size() + ", Hash: " + hash);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
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
