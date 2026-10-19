package com.browser.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PageSourceDialog {
    public static void show(Stage ownerStage, String title, String htmlSource) {
        Stage dialog = new Stage();
        dialog.initOwner(ownerStage);
        dialog.initModality(Modality.NONE);
        dialog.setTitle("Page Source - " + (title != null ? title : "Source"));

        VBox root = new VBox(10);
        root.setPadding(new Insets(14));
        root.getStyleClass().add("dialog-root");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Source: " + title);
        titleLabel.getStyleClass().add("dialog-title");
        titleLabel.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_CODE, 16, "#38bdf8"));
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Button copyBtn = new Button("Copy All");
        copyBtn.getStyleClass().add("primary-btn");
        copyBtn.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(htmlSource);
            clipboard.setContent(content);
            copyBtn.setText("Copied!");
        });

        header.getChildren().addAll(titleLabel, copyBtn);

        TextArea textArea = new TextArea(htmlSource);
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.getStyleClass().add("code-text-area");
        VBox.setVgrow(textArea, Priority.ALWAYS);

        root.getChildren().addAll(header, textArea);

        Scene scene = new Scene(root, 780, 560);
        if (ownerStage.getScene() != null && !ownerStage.getScene().getStylesheets().isEmpty()) {
            scene.getStylesheets().addAll(ownerStage.getScene().getStylesheets());
        }
        dialog.setScene(scene);
        dialog.show();
    }
}
