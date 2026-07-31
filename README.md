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

# OS Support Matrix

This application uses native Java AWT bindings for system clipboard and system tray interactions, wrapped in a lightweight JavaFX UI.

| OS | Status | Notes |
|----|--------|-------|
| **macOS** (10.15+) | ✅ Supported | Full support for tray icon and clipboard access. |
| **Windows** (10+) | ✅ Supported | Full support for tray icon and clipboard access. |
| **Linux (X11)** | ✅ Supported | Requires `xclip` or `xsel` installed for optimal clipboard monitoring. |
| **Linux (Wayland)** | ⚠️ Limited | **Known Wayland Limitation:** Wayland's strict security model actively prevents background applications from reading the global clipboard unless the application explicitly has focus. Java's AWT `SystemTray` API also lacks native Wayland support (often resulting in missing icons or falling back to a legacy XWayland window). Running the application under XWayland (`GDK_BACKEND=x11`) is required for functional clipboard tracking on Linux. |

# Exporting the Application

You can package this application into a native executable (like `.dmg` on Mac, `.exe` on Windows, or `.deb` on Linux) using the built-in JDK `jpackage` tool.

1. **Build the Fat JAR:**
   First, compile the application using Maven:
   ```bash
   ./mvnw clean package
   ```
   *This creates `target/Clipboard-History-Manager-0.0.1-SNAPSHOT.jar`.*

2. **Run JPackage:**
   Execute the following command based on your OS (you must run this on the target OS).

   **macOS (.dmg):**
   ```bash
   jpackage --name "ClipboardManager" \
     --input target/ \
     --main-jar Clipboard-History-Manager-0.0.1-SNAPSHOT.jar \
     --type dmg
   ```

   **Windows (.exe):**
   ```powershell
   jpackage --name "ClipboardManager" `
     --input target/ `
     --main-jar Clipboard-History-Manager-0.0.1-SNAPSHOT.jar `
     --win-shortcut `
     --type exe
   ```

   **Linux (.deb):**
   ```bash
   jpackage --name "clipboard-manager" \
     --input target/ \
     --main-jar Clipboard-History-Manager-0.0.1-SNAPSHOT.jar \
     --linux-shortcut \
     --type deb
   ```