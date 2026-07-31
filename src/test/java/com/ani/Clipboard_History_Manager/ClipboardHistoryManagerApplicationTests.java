package com.ani.Clipboard_History_Manager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ClipboardHistoryManagerApplicationTests {

	@BeforeAll
	static void setup() {
		System.setProperty("java.awt.headless", "false");
		try {
			javafx.application.Platform.startup(() -> {});
		} catch (IllegalStateException e) {
			// Toolkit already initialized
		}
	}

	@Test
	void contextLoads() {
	}

}
