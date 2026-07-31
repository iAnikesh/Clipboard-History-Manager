package com.ani.Clipboard_History_Manager.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LRUCacheTest {

    private LRUCache<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = new LRUCache<>(3); // Capacity of 3 for testing
    }

    @Test
    void testBasicPutAndGet() {
        cache.put("1", "One");
        cache.put("2", "Two");

        assertEquals("One", cache.get("1"));
        assertEquals("Two", cache.get("2"));
        assertNull(cache.get("3"));
        assertEquals(2, cache.size());
    }

    @Test
    void testEvictionPolicy() {
        cache.put("1", "One");
        cache.put("2", "Two");
        cache.put("3", "Three");
        // Cache is full now: [3, 2, 1]

        cache.put("4", "Four");
        // "1" should be evicted: [4, 3, 2]

        assertNull(cache.get("1"), "Key '1' should have been evicted");
        assertEquals("Two", cache.get("2"));
        assertEquals("Three", cache.get("3"));
        assertEquals("Four", cache.get("4"));
        assertEquals(3, cache.size());
    }

    @Test
    void testAccessUpdatesLRU() {
        cache.put("1", "One");
        cache.put("2", "Two");
        cache.put("3", "Three");
        // Cache: [3, 2, 1]

        // Access "1", moving it to most recently used: [1, 3, 2]
        cache.get("1");

        // Add "4", which should evict "2" (least recently used now): [4, 1, 3]
        cache.put("4", "Four");

        assertNull(cache.get("2"), "Key '2' should have been evicted");
        assertEquals("One", cache.get("1"));
        assertEquals("Three", cache.get("3"));
        assertEquals("Four", cache.get("4"));
    }

    @Test
    void testUpdateExistingKey() {
        cache.put("1", "One");
        cache.put("2", "Two");
        cache.put("1", "OneUpdated");
        // Cache should be: [1, 2]

        assertEquals("OneUpdated", cache.get("1"));
        assertEquals(2, cache.size());

        cache.put("3", "Three");
        cache.put("4", "Four");
        // Cache should be [4, 3, 1]. "2" should be evicted.
        
        assertNull(cache.get("2"));
        assertEquals("OneUpdated", cache.get("1"));
    }

    @Test
    void testRemove() {
        cache.put("1", "One");
        cache.put("2", "Two");
        cache.remove("1");
        
        assertNull(cache.get("1"));
        assertEquals(1, cache.size());
    }

    @Test
    void testGetAllMostRecentFirst() {
        cache.put("1", "One");
        cache.put("2", "Two");
        cache.put("3", "Three");

        cache.get("2"); // makes 2 most recent
        
        List<String> list = cache.getAllMostRecentFirst();
        assertEquals(3, list.size());
        assertEquals("Two", list.get(0));
        assertEquals("Three", list.get(1));
        assertEquals("One", list.get(2));
    }

    @Test
    void testInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new LRUCache<>(0));
        assertThrows(IllegalArgumentException.class, () -> new LRUCache<>(-1));
    }

    @Test
    void testDeduplication() {
        cache.put("hash1", "Repeated Text");
        cache.put("hash1", "Repeated Text");
        cache.put("hash1", "Repeated Text");
        
        assertEquals(1, cache.size(), "Cache size should not grow on duplicate inserts");
        assertEquals("Repeated Text", cache.get("hash1"));
        
        // Ensure adding a new item doesn't interfere
        cache.put("hash2", "New Text");
        assertEquals(2, cache.size());
        assertEquals("New Text", cache.get("hash2"));
        
        // Re-adding hash1 should just move it to most recently used
        cache.put("hash1", "Repeated Text");
        assertEquals(2, cache.size());
        
        List<String> list = cache.getAllMostRecentFirst();
        assertEquals("Repeated Text", list.get(0));
        assertEquals("New Text", list.get(1));
    }
}
