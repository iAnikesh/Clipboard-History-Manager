package com.ani.Clipboard_History_Manager.clipboard;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Entity
public class ClipboardItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private long timestamp;
    
    // Hash is used to uniquely identify/dedupe this entry
    @Column(unique = true, nullable = false)
    private String hash;

    public ClipboardItem() {
        // JPA requires a no-arg constructor
    }

    public ClipboardItem(String content, long timestamp, String hash) {
        this.content = content;
        this.timestamp = timestamp;
        this.hash = hash;
    }

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getHash() {
        return hash;
    }
    
    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "ClipboardItem{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", hash='" + hash + '\'' +
                '}';
    }
}
