package com.ani.Clipboard_History_Manager.clipboard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClipboardItemRepository extends JpaRepository<ClipboardItem, Long> {
    ClipboardItem findByHash(String hash);
}
