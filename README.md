# Clipboard History Manager

A cross-platform desktop clipboard manager built with **Spring Boot** and **JavaFX**. It watches the system clipboard in the background, deduplicates and stores history persistently, and lets you browse and re-copy past entries from a lightweight UI and system tray icon.

## Features

- **Background clipboard watching** — a virtual thread (`Thread.ofVirtual()`) monitors the system clipboard via `Toolkit.getDefaultToolkit().getSystemClipboard()`, with a `FlavorListener` fast path and a polling fallback for platforms where flavor events are unreliable.
- **Producer-consumer pipeline** — the watcher publishes detected changes onto a `BlockingQueue`, decoupling clipboard capture from processing so the UI thread is never blocked.
- **Duplicate detection** — each entry is hashed with SHA-256 before being cached or persisted, so repeated copies of the same content don't create redundant history.
- **Custom LRU cache** — a hand-written `HashMap` + doubly linked list implementation with O(1) `get`/`put`/`evict`, covered by JUnit 5 tests for eviction order and recency behavior.
- **Persistent history** — Spring Data JPA + an embedded H2 file database store clipboard entries across restarts; the cache is rehydrated from disk on startup.
- **Desktop UI** — a JavaFX list view (embedded in a Spring application context) shows recent clipboard entries, refreshing live via an observer-style listener whenever the cache updates. Click an entry to copy it back to the system clipboard.
- **System tray integration** — runs quietly in the background with a tray icon so the window can be hidden without closing the app.
- **CI** — GitHub Actions runs `mvn test` on every push and pull request to `main`/`master`.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Application framework | Spring Boot 4.1 (Spring Data JPA, dependency injection) |
| UI | JavaFX 25 |
| Database | H2 (file-based, embedded) |
| Concurrency | Java virtual threads, `BlockingQueue` |
| Testing | JUnit 5 |
| CI | GitHub Actions |
| Build | Maven |

## Architecture

```
System Clipboard
      │
      ▼
ClipboardWatcher (virtual thread)
      │  puts ClipboardItem
      ▼
BlockingQueue<ClipboardItem>
      │  take()
      ▼
ClipboardConsumer (virtual thread)
      │  SHA-256 hash → dedupe check
      ├──────────────► LRUCache<hash, ClipboardItem>  (in-memory, O(1) ops)
      └──────────────► ClipboardItemRepository (Spring Data JPA → H2)
      │
      ▼  notifies listeners
JavaFX UI (ClipboardHistoryController)
      │
      └──► System tray (SystemTrayManager)
```

On startup, existing history is loaded from H2 back into the LRU cache so recent items are available immediately without waiting for new clipboard activity.

## Getting Started

### Prerequisites
- JDK 25
- Maven (or use the included `./mvnw` wrapper — no local Maven install needed)

### Run locally
```bash
./mvnw spring-boot:run
```
This launches the JavaFX window with the background watcher and consumer running inside the same Spring context.

### Run tests
```bash
./mvnw test
```
Covers the LRU cache's core behavior: basic put/get, eviction under capacity, and recency updates on access.

## Packaging a Native Executable

Build the runnable JAR first:
```bash
./mvnw clean package
```
This produces `target/Clipboard-History-Manager-0.0.1-SNAPSHOT.jar`.

Then use the JDK's built-in `jpackage` tool to create a native installer (run on the target OS):

**macOS (.dmg)**
```bash
jpackage --name "ClipboardManager" \
  --input target/ \
  --main-jar Clipboard-History-Manager-0.0.1-SNAPSHOT.jar \
  --type dmg
```

**Windows (.exe)**
```powershell
jpackage --name "ClipboardManager" `
  --input target/ `
  --main-jar Clipboard-History-Manager-0.0.1-SNAPSHOT.jar `
  --win-shortcut `
  --type exe
```

**Linux (.deb)**
```bash
jpackage --name "clipboard-manager" \
  --input target/ \
  --main-jar Clipboard-History-Manager-0.0.1-SNAPSHOT.jar \
  --linux-shortcut \
  --type deb
```

## OS Support Matrix

The app uses native Java AWT bindings for clipboard and system tray access, wrapped in a JavaFX UI.

| OS | Status | Notes |
|----|--------|-------|
| **macOS** (10.15+) | ✅ Supported | Full tray icon and clipboard access. |
| **Windows** (10+) | ✅ Supported | Full tray icon and clipboard access. |
| **Linux (X11)** | ✅ Supported | Requires `xclip` or `xsel` for reliable clipboard monitoring. |
| **Linux (Wayland)** | ⚠️ Limited | Wayland's security model blocks background apps from reading the global clipboard unless focused, and Java AWT's `SystemTray` API has no native Wayland support. Run under XWayland (`GDK_BACKEND=x11`) for functional clipboard tracking. |

## Project Structure

```
src/main/java/com/ani/Clipboard_History_Manager/
├── ClipboardHistoryManagerApplication.java   # Spring Boot entry point
├── JavaFxApplication.java                    # Bridges JavaFX lifecycle with Spring context
├── PrimaryStageInitializer.java
├── StageReadyEvent.java
├── cache/
│   └── LRUCache.java                         # Generic O(1) LRU cache
├── clipboard/
│   ├── ClipboardWatcher.java                 # Virtual-thread clipboard polling/listening
│   ├── ClipboardConsumer.java                # Hashing, dedup, cache + DB write-through
│   ├── ClipboardItem.java                    # JPA entity
│   └── ClipboardItemRepository.java          # Spring Data JPA repository
└── ui/
    ├── ClipboardHistoryController.java       # JavaFX list view + click-to-copy
    └── SystemTrayManager.java                # Tray icon lifecycle
```

## Roadmap

Planned but not yet implemented:
- **Prefix search** over clipboard history (trie-based, for O(k) lookups instead of a linear scan)
- **Global hotkey paste** — a system-wide shortcut to paste a specific history index without opening the app window
