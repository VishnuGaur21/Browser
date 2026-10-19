package com.browser.ui;

import com.browser.model.DownloadItem;
import com.browser.service.DownloadService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;

public class DownloadsDialog {
    public static void show(Stage ownerStage, DownloadService downloadService) {
        Stage dialog = new Stage();
        dialog.initOwner(ownerStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Downloads");

        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.getStyleClass().add("dialog-root");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Downloads");
        titleLabel.getStyleClass().add("dialog-title");
        titleLabel.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_DOWNLOAD, 18, "#10b981"));
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Button openFolderBtn = new Button("Open Downloads Folder");
        openFolderBtn.getStyleClass().add("primary-btn");
        openFolderBtn.setOnAction(e -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(downloadService.getDownloadFolder());
                }
            } catch (Exception ex) {
                System.err.println("Failed to open download folder: " + ex.getMessage());
            }
        });

        header.getChildren().addAll(titleLabel, openFolderBtn);

        // List
        ListView<DownloadItem> listView = new ListView<>(downloadService.getDownloads());
        listView.getStyleClass().add("dialog-list-view");
        VBox.setVgrow(listView, Priority.ALWAYS);

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(DownloadItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox card = new VBox(4);
                    card.setPadding(new Insets(4, 2, 4, 2));

                    HBox topRow = new HBox(8);
                    topRow.setAlignment(Pos.CENTER_LEFT);

                    Label nameLabel = new Label(item.getFileName());
                    nameLabel.getStyleClass().add("download-filename");
                    HBox.setHgrow(nameLabel, Priority.ALWAYS);

                    Label statusLabel = new Label(item.getStatus().name());
                    statusLabel.getStyleClass().add("download-status");

                    topRow.getChildren().addAll(nameLabel, statusLabel);

                    ProgressBar pBar = new ProgressBar(item.getProgress());
                    pBar.setMaxWidth(Double.MAX_VALUE);
                    pBar.getStyleClass().add("download-progress");

                    card.getChildren().addAll(topRow, pBar);
                    setGraphic(card);
                }
            }
        });

        root.getChildren().addAll(header, listView);

        Scene scene = new Scene(root, 580, 420);
        if (ownerStage.getScene() != null && !ownerStage.getScene().getStylesheets().isEmpty()) {
            scene.getStylesheets().addAll(ownerStage.getScene().getStylesheets());
        }
        dialog.setScene(scene);
        dialog.show();
    }
}
