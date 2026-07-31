package com.ani.Clipboard_History_Manager;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.ani.Clipboard_History_Manager.ui.ClipboardHistoryController;

@Component
public class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {

    private final ClipboardHistoryController historyController;

    public PrimaryStageInitializer(ClipboardHistoryController historyController) {
        this.historyController = historyController;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getStage();
        
        // Use a fixed size that looks like a popup menu
        Scene scene = new Scene(historyController.getView(), 350, 450);
        
        stage.setScene(scene);
        stage.initStyle(javafx.stage.StageStyle.UNDECORATED); // Remove window decorations
        stage.setAlwaysOnTop(true);
        
        // Hide when the window loses focus (like a true tray popup)
        stage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                stage.hide();
            }
        });
        
        // Notice we do NOT call stage.show() here.
        // It starts hidden in the system tray.
    }
}
