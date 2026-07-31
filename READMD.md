# System design summary
- The pipeline is intentionally layered so each stage maps to one interview talking point:

## Clipboard watcher — a virtual thread polls the OS clipboard every ~500ms. Virtual threads matter here because they're cheap enough that background polling/I/O doesn't cost you a platform thread.
## Blocking queue — decouples the watcher (producer) from processing (consumer) — classic producer-consumer pattern.
## Consumer workers — pull off the queue, compute a SHA-256 hash for O(1) duplicate detection before anything touches the cache.
## LRU cache — your DSA centerpiece: HashMap + doubly linked list, O(1) get/put, capacity-bounded.
## Trie index — built alongside the cache for O(k) prefix search (k = query length) instead of scanning history linearly.
## Persistence layer — Spring Data JPA writes through to H2 so history survives restarts.
## JavaFX UI — observes the service layer (Observer pattern), renders the list, handles click-to-copy and search.

# 7-day roadmap

## Day 1 — Scaffold
- Maven project, add Spring Boot + JavaFX dependencies, get a minimal JavaFX window launching inside a Spring context (this integration is slightly fiddly — budget time for it). Push initial commit, set up GitHub Actions to run mvn test on push.

## Day 2 — Concurrency pipeline
- Implement the clipboard watcher on a virtual thread reading via Toolkit.getDefaultToolkit().getSystemClipboard(). Feed detected changes into a BlockingQueue<ClipboardItem>. Verify with console logging before touching any UI.

## Day 3 — LRU cache (your centerpiece)
- Build the LRU cache from scratch: HashMap<String, Node> + doubly linked list, O(1) get/put/evict. Write thorough JUnit 5 tests for it in isolation — this class alone should be interview-ready by end of day. Wire consumer workers to compute SHA-256 hashes and dedupe before inserting.

## Day 4 — Persistence
- Add Spring Data JPA entity ClipboardItem, repository interface, H2 file-based datasource config. Service layer writes through to DB on insert and loads history into the cache on startup.

## Day 5 (was: Trie search) → 
- Add JNativeHook dependency, implement the global hotkey listener with held-key tracking for the Ctrl+V+[digit] chord, and build the paste dispatcher that reads index i from the LRU cache, sets it as the system clipboard, then fires Robot to simulate Ctrl+V into the focused app.

## Day 6 → JavaFX UI: 
- list view showing index numbers 0–9 next to hotkey-eligible items, 10–29 shown but visually distinguished as scroll-only. System tray icon.

## Day 7 → 
- Tests for the LRU cache and dedup logic, CI, README documenting the OS support matrix (this is a good place to show you understand the Wayland limitation rather than hide it), jpackage build.