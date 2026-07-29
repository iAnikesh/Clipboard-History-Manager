package com.ani.Clipboard_History_Manager;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getStage();
        
        StackPane root = new StackPane();
        root.getChildren().add(new Label("Hello, JavaFX + Spring Boot!"));
        Scene scene = new Scene(root, 400, 300);
        
        stage.setScene(scene);
        stage.setTitle("Clipboard History Manager");
        stage.show();
    }
}
