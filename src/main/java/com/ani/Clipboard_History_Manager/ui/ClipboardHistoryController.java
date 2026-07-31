package com.ani.Clipboard_History_Manager.ui;

import com.ani.Clipboard_History_Manager.clipboard.ClipboardConsumer;
import com.ani.Clipboard_History_Manager.clipboard.ClipboardItem;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.List;

@Component
public class ClipboardHistoryController {

    private final ClipboardConsumer consumer;
    private final VBox view;
    private final ListView<ClipboardItem> listView;
    private final ObservableList<ClipboardItem> itemsList;

    public ClipboardHistoryController(ClipboardConsumer consumer) {
        this.consumer = consumer;
        this.itemsList = FXCollections.observableArrayList();
        this.listView = new ListView<>(itemsList);
        
        this.view = new VBox(listView);
        this.view.setPadding(new Insets(10));
        VBox.setVgrow(listView, Priority.ALWAYS);
        
        setupListView();
        
        // Initial load
        refreshList();
        
        // Listen to changes from background watcher
        consumer.addListener(() -> Platform.runLater(this::refreshList));
    }

    public VBox getView() {
        return view;
    }

    private void refreshList() {
        List<ClipboardItem> allItems = consumer.getCache().getAllMostRecentFirst();
        itemsList.setAll(allItems);
    }

    private void setupListView() {
        // Modern styling
        view.setStyle("-fx-background-color: #f5f5f7;");
        listView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-border-width: 0;");
        
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ClipboardItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    int index = getIndex();
                    String content = item.getContent();
                    // Max length 50 chars for display
                    if (content.length() > 50) {
                        content = content.substring(0, 50).replace('\n', ' ') + "...";
                    } else {
                        content = content.replace('\n', ' ');
                    }
                    
                    String baseStyle = "-fx-padding: 10px; -fx-background-radius: 5px; ";
                    if (isSelected()) {
                        baseStyle += "-fx-background-color: #007aff; -fx-text-fill: white; ";
                    } else {
                        baseStyle += "-fx-background-color: transparent; -fx-text-fill: #1d1d1f;";
                    }
                    
                    setText(content);
                    setStyle(baseStyle);
                }
            }
        });

        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ClipboardItem selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    copyToClipboardAndHide(selected);
                }
            }
        });
        
        listView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ClipboardItem selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    copyToClipboardAndHide(selected);
                }
            }
        });
    }

    private void copyToClipboardAndHide(ClipboardItem item) {
        StringSelection selection = new StringSelection(item.getContent());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        
        // Hide window
        if (view.getScene() != null && view.getScene().getWindow() != null) {
            view.getScene().getWindow().hide();
        }
    }
}
