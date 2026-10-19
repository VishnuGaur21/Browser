package com.browser.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

import java.util.function.Supplier;

public class FindInPageBar extends HBox {
    private final Supplier<BrowserTab> activeTabSupplier;
    private final Runnable onClose;

    private final TextField searchField;
    private final Button prevBtn;
    private final Button nextBtn;
    private final Button closeBtn;
    private final Label statusLabel;

    public FindInPageBar(Supplier<BrowserTab> activeTabSupplier, Runnable onClose) {
        this.activeTabSupplier = activeTabSupplier;
        this.onClose = onClose;

        setAlignment(Pos.CENTER_LEFT);
        setSpacing(6);
        setPadding(new Insets(6, 12, 6, 12));
        getStyleClass().add("find-bar-root");

        searchField = new TextField();
        searchField.setPromptText("Find in page...");
        searchField.getStyleClass().add("find-text-field");
        searchField.setPrefWidth(180);

        statusLabel = new Label();
        statusLabel.getStyleClass().add("find-status-label");

        prevBtn = new Button();
        prevBtn.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_BACK, 12, "#94a3b8"));
        prevBtn.getStyleClass().add("find-btn");
        prevBtn.setTooltip(new Tooltip("Previous match (Shift+Enter)"));

        nextBtn = new Button();
        nextBtn.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_FORWARD, 12, "#94a3b8"));
        nextBtn.getStyleClass().add("find-btn");
        nextBtn.setTooltip(new Tooltip("Next match (Enter)"));

        closeBtn = new Button();
        closeBtn.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_CLOSE, 11, "#94a3b8"));
        closeBtn.getStyleClass().add("find-btn");
        closeBtn.setTooltip(new Tooltip("Close (Esc)"));

        getChildren().addAll(searchField, statusLabel, prevBtn, nextBtn, closeBtn);

        // Actions
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                executeFind(true);
            }
        });

        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                executeFind(!e.isShiftDown());
            } else if (e.getCode() == KeyCode.ESCAPE) {
                close();
            }
        });

        nextBtn.setOnAction(e -> executeFind(true));
        prevBtn.setOnAction(e -> executeFind(false));
        closeBtn.setOnAction(e -> close());
    }

    private void executeFind(boolean forward) {
        BrowserTab tab = activeTabSupplier.get();
        if (tab != null) {
            String text = searchField.getText().trim();
            if (!text.isBlank()) {
                boolean found = tab.findInPage(text, forward);
                statusLabel.setText(found ? "Match" : "No match");
            } else {
                statusLabel.setText("");
            }
        }
    }

    public void focusAndSelect() {
        searchField.requestFocus();
        searchField.selectAll();
    }

    public void close() {
        searchField.clear();
        statusLabel.setText("");
        if (onClose != null) onClose.run();
    }
}
