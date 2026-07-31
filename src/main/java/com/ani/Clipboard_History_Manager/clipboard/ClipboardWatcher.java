package com.ani.Clipboard_History_Manager.clipboard;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class ClipboardWatcher {

    private final BlockingQueue<ClipboardItem> queue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;
    private Thread watcherThread;

    @PostConstruct
    public void startWatching() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        
        // Fast path: attempt to use FlavorListener
        clipboard.addFlavorListener(e -> checkClipboard(clipboard));

        // Slow path fallback: poll less frequently (1500ms instead of 500ms) to reduce lag
        watcherThread = Thread.ofVirtual().start(() -> {
            System.out.println("Clipboard watcher started on virtual thread: " + Thread.currentThread());
            while (running) {
                checkClipboard(clipboard);
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    private String lastContent = "";

    private synchronized void checkClipboard(Clipboard clipboard) {
        try {
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                String currentContent = (String) clipboard.getData(DataFlavor.stringFlavor);
                if (currentContent != null && !currentContent.equals(lastContent)) {
                    lastContent = currentContent;
                    ClipboardItem item = new ClipboardItem();
                    item.setContent(currentContent);
                    item.setTimestamp(System.currentTimeMillis());
                    queue.put(item);
                    System.out.println("New clipboard item detected: " + item.getContent());
                }
            }
        } catch (Exception e) {
            // Ignore transient errors reading clipboard
        }
    }

    @PreDestroy
    public void stopWatching() {
        running = false;
        if (watcherThread != null) {
            watcherThread.interrupt();
        }
    }

    public BlockingQueue<ClipboardItem> getQueue() {
        return queue;
    }
}
