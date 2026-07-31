package com.ani.Clipboard_History_Manager.ui;

import com.ani.Clipboard_History_Manager.StageReadyEvent;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;

@Component
public class SystemTrayManager implements ApplicationListener<StageReadyEvent> {

    private Stage primaryStage;

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        this.primaryStage = event.getStage();
        // Prevent JavaFX from closing when the main window is hidden
        Platform.setImplicitExit(false);
        
        // AWT UI must be created on AWT Event Dispatch Thread or carefully.
        // It's generally safe here since we enabled java.awt.headless=false
        EventQueue.invokeLater(this::setupSystemTray);
    }

    private void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        
        Image image = null;
        try {
            java.net.URL url = getClass().getResource("/tray-icon.png");
            if (url != null) {
                image = javax.imageio.ImageIO.read(url);
                image = image.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (image == null) {
            // fallback
            BufferedImage bImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bImage.createGraphics();
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, 16, 16);
            g2d.setColor(Color.WHITE);
            g2d.drawString("CH", 1, 12);
            g2d.dispose();
            image = bImage;
        }

        TrayIcon trayIcon = new TrayIcon(image, "Clipboard History Manager");
        trayIcon.setImageAutoSize(true);

        // AWT Popup Menu
        PopupMenu popup = new PopupMenu();
        
        MenuItem showItem = new MenuItem("Show History");
        showItem.addActionListener(e -> Platform.runLater(() -> {
            primaryStage.show();
            primaryStage.toFront();
        }));
        
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            Platform.exit();
            System.exit(0);
        });

        popup.add(showItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);
        
        // Click on icon shows the window
        trayIcon.addActionListener(e -> Platform.runLater(() -> {
            primaryStage.show();
            primaryStage.toFront();
        }));

        try {
            tray.add(trayIcon);
            System.out.println("System tray icon added.");
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }
}
