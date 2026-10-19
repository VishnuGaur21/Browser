package com.browser.ui;

import com.browser.model.HistoryItem;
import com.browser.service.HistoryManager;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class HistoryDialog {
    public static void show(Stage ownerStage, HistoryManager historyManager, Consumer<String> onNavigate) {
        Stage dialog = new Stage();
        dialog.initOwner(ownerStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Browsing History");

        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.getStyleClass().add("dialog-root");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Browsing History");
        titleLabel.getStyleClass().add("dialog-title");
        titleLabel.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_HISTORY, 18, "#38bdf8"));
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Button clearBtn = new Button("Clear History");
        clearBtn.getStyleClass().add("danger-btn");
        clearBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to clear all browsing history?", ButtonType.YES, ButtonType.NO);
            alert.initOwner(dialog);
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    historyManager.clearHistory();
                }
            });
        });

        header.getChildren().addAll(titleLabel, clearBtn);

        // Search Field
        TextField searchField = new TextField();
        searchField.setPromptText("Search history by title or URL...");
        searchField.getStyleClass().add("dialog-search-field");

        // Filtered List
        FilteredList<HistoryItem> filtered = new FilteredList<>(historyManager.getHistory(), p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filtered.setPredicate(item -> {
                if (newVal == null || newVal.isBlank()) return true;
                String lower = newVal.toLowerCase();
                return item.getTitle().toLowerCase().contains(lower) || item.getUrl().toLowerCase().contains(lower);
            });
        });

        // ListView
        ListView<HistoryItem> listView = new ListView<>(filtered);
        listView.getStyleClass().add("dialog-list-view");
        VBox.setVgrow(listView, Priority.ALWAYS);

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(HistoryItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);

                    Label timeLabel = new Label(item.getTime());
                    timeLabel.getStyleClass().add("history-time-label");
                    timeLabel.setMinWidth(110);

                    VBox contentBox = new VBox(2);
                    Label title = new Label(item.getTitle());
                    title.getStyleClass().add("history-title-label");

                    Label url = new Label(item.getUrl());
                    url.getStyleClass().add("history-url-label");

                    contentBox.getChildren().addAll(title, url);
                    HBox.setHgrow(contentBox, Priority.ALWAYS);

                    Button deleteBtn = new Button();
                    deleteBtn.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_CLOSE, 10, "#94a3b8"));
                    deleteBtn.getStyleClass().add("dialog-item-delete-btn");
                    deleteBtn.setOnAction(ev -> {
                        historyManager.removeEntry(item);
                    });

                    box.getChildren().addAll(timeLabel, contentBox, deleteBtn);
                    setGraphic(box);
                }
            }
        });

        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                HistoryItem selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    onNavigate.accept(selected.getUrl());
                    dialog.close();
                }
            }
        });

        root.getChildren().addAll(header, searchField, listView);

        Scene scene = new Scene(root, 650, 480);
        if (ownerStage.getScene() != null && !ownerStage.getScene().getStylesheets().isEmpty()) {
            scene.getStylesheets().addAll(ownerStage.getScene().getStylesheets());
        }
        dialog.setScene(scene);
        dialog.show();
    }
}
