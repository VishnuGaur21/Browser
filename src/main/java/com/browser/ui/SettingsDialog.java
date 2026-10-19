package com.browser.ui;

import com.browser.service.BookmarkManager;
import com.browser.service.ConfigManager;
import com.browser.service.HistoryManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SettingsDialog {
    public static void show(Stage ownerStage, ConfigManager configManager, HistoryManager historyManager, BookmarkManager bookmarkManager) {
        Stage dialog = new Stage();
        dialog.initOwner(ownerStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Settings - Modern Browser");

        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("dialog-root");

        // Header
        Label titleLabel = new Label("Browser Settings");
        titleLabel.getStyleClass().add("dialog-title");
        titleLabel.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_SETTINGS, 18, "#818cf8"));

        // Search Engine Section
        VBox engineSection = new VBox(6);
        Label engineLabel = new Label("Default Search Engine");
        engineLabel.getStyleClass().add("settings-section-title");

        ComboBox<String> engineCombo = new ComboBox<>();
        engineCombo.getItems().addAll("Google", "DuckDuckGo", "Bing", "Brave");
        String currentEngine = configManager.getSearchEngine();
        if (currentEngine.equalsIgnoreCase("duckduckgo")) engineCombo.setValue("DuckDuckGo");
        else if (currentEngine.equalsIgnoreCase("bing")) engineCombo.setValue("Bing");
        else if (currentEngine.equalsIgnoreCase("brave")) engineCombo.setValue("Brave");
        else engineCombo.setValue("Google");

        engineCombo.setOnAction(e -> {
            configManager.setSearchEngine(engineCombo.getValue().toLowerCase());
        });
        engineSection.getChildren().addAll(engineLabel, engineCombo);

        // Home Page Section
        VBox homeSection = new VBox(6);
        Label homeLabel = new Label("New Tab & Home Page");
        homeLabel.getStyleClass().add("settings-section-title");

        TextField homeField = new TextField(configManager.getHomePage());
        homeField.setPromptText("startpage.html or custom URL");
        homeField.getStyleClass().add("dialog-search-field");
        homeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                configManager.setHomePage(newVal.trim());
            }
        });
        homeSection.getChildren().addAll(homeLabel, homeField);

        // Gemini AI API Key Section
        VBox geminiSection = new VBox(6);
        Label geminiLabel = new Label("Google Gemini AI API Key");
        geminiLabel.getStyleClass().add("settings-section-title");

        Label geminiDesc = new Label("Used by the built-in Gemini Copilot sidebar for webpage summaries, explanations, and questions.");
        geminiDesc.getStyleClass().add("settings-desc");
        geminiDesc.setWrapText(true);

        HBox keyRow = new HBox(8);
        PasswordField keyField = new PasswordField();
        keyField.setText(configManager.getGeminiApiKey());
        keyField.setPromptText("AIzaSy...");
        keyField.getStyleClass().add("dialog-search-field");
        HBox.setHgrow(keyField, Priority.ALWAYS);

        Button saveKeyBtn = new Button("Save Key");
        saveKeyBtn.getStyleClass().add("primary-btn");
        saveKeyBtn.setOnAction(e -> {
            configManager.setGeminiApiKey(keyField.getText().trim());
            saveKeyBtn.setText("Saved!");
        });
        keyRow.getChildren().addAll(keyField, saveKeyBtn);
        geminiSection.getChildren().addAll(geminiLabel, geminiDesc, keyRow);

        // Privacy & Data
        VBox privacySection = new VBox(8);
        Label privacyLabel = new Label("Privacy & Browsing Data");
        privacyLabel.getStyleClass().add("settings-section-title");

        HBox privacyBtnRow = new HBox(10);
        Button clearHistBtn = new Button("Clear History");
        clearHistBtn.getStyleClass().add("danger-btn");
        clearHistBtn.setOnAction(e -> {
            historyManager.clearHistory();
            clearHistBtn.setText("History Cleared");
        });

        privacyBtnRow.getChildren().addAll(clearHistBtn);
        privacySection.getChildren().addAll(privacyLabel, privacyBtnRow);

        // Footer Done Button
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button doneBtn = new Button("Done");
        doneBtn.getStyleClass().add("primary-btn");
        doneBtn.setOnAction(e -> dialog.close());
        footer.getChildren().add(doneBtn);

        root.getChildren().addAll(titleLabel, new Separator(), engineSection, homeSection, geminiSection, privacySection, new Separator(), footer);

        Scene scene = new Scene(root, 540, 520);
        if (ownerStage.getScene() != null && !ownerStage.getScene().getStylesheets().isEmpty()) {
            scene.getStylesheets().addAll(ownerStage.getScene().getStylesheets());
        }
        dialog.setScene(scene);
        dialog.show();
    }
}
