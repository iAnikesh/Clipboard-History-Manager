package com.ani.Clipboard_History_Manager.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class LRUCache<K, V> {

    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int capacity;
    private final Map<K, Node<K, V>> cache;
    private final Node<K, V> head;
    private final Node<K, V> tail;

    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.capacity = capacity;
        this.cache = new HashMap<>();
        
        // Dummy head and tail nodes to avoid null checks
        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }

    public synchronized V get(K key) {
        Node<K, V> node = cache.get(key);
        if (node == null) {
            return null;
        }
        // Move to head (most recently used)
        moveToHead(node);
        return node.value;
    }

    public synchronized void put(K key, V value) {
        Node<K, V> node = cache.get(key);
        if (node != null) {
            // Update existing node and move to head
            node.value = value;
            moveToHead(node);
        } else {
            // Create new node
            Node<K, V> newNode = new Node<>(key, value);
            cache.put(key, newNode);
            addToHead(newNode);

            // Check capacity
            if (cache.size() > capacity) {
                Node<K, V> lru = removeTail();
                cache.remove(lru.key);
            }
        }
    }

    public synchronized void remove(K key) {
        Node<K, V> node = cache.get(key);
        if (node != null) {
            removeNode(node);
            cache.remove(key);
        }
    }

    public synchronized int size() {
        return cache.size();
    }

    public synchronized void clear() {
        cache.clear();
        head.next = tail;
        tail.prev = head;
    }
    
    public synchronized List<V> getAllMostRecentFirst() {
        List<V> list = new ArrayList<>();
        Node<K, V> curr = head.next;
        while (curr != tail) {
            list.add(curr.value);
            curr = curr.next;
        }
        return list;
    }

    private void addToHead(Node<K, V> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void moveToHead(Node<K, V> node) {
        removeNode(node);
        addToHead(node);
    }

    private Node<K, V> removeTail() {
        Node<K, V> res = tail.prev;
        removeNode(res);
        return res;
    }
}
