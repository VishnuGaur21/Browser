package com.browser.ui;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.util.function.Consumer;

public class TabBarView extends HBox {
    private final HBox tabsContainer;
    private final ScrollPane scrollPane;
    private final Button newTabBtn;
    private final ObservableList<BrowserTab> tabs;
    private BrowserTab selectedTab;

    private Consumer<BrowserTab> onTabSelected;
    private Consumer<BrowserTab> onTabClosed;
    private Runnable onNewTabRequested;

    public TabBarView(ObservableList<BrowserTab> tabs) {
        this.tabs = tabs;

        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(8, 12, 0, 12));
        setSpacing(6);
        getStyleClass().add("tab-bar-root");

        tabsContainer = new HBox(6);
        tabsContainer.setAlignment(Pos.CENTER_LEFT);

        scrollPane = new ScrollPane(tabsContainer);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("tabs-scroll-pane");
        HBox.setHgrow(scrollPane, Priority.ALWAYS);

        // Plus button
        newTabBtn = new Button();
        newTabBtn.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_PLUS, 14, "#94a3b8"));
        newTabBtn.getStyleClass().add("new-tab-btn");
        newTabBtn.setTooltip(new Tooltip("New Tab (Ctrl+T)"));
        newTabBtn.setOnAction(e -> {
            if (onNewTabRequested != null) onNewTabRequested.run();
        });

        getChildren().addAll(scrollPane, newTabBtn);

        // Listen for tab list changes
        tabs.addListener((ListChangeListener<BrowserTab>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (BrowserTab tab : c.getAddedSubList()) {
                        addTabUI(tab);
                    }
                }
                if (c.wasRemoved()) {
                    for (BrowserTab tab : c.getRemoved()) {
                        removeTabUI(tab);
                    }
                }
            }
        });
    }

    public void selectTab(BrowserTab tab) {
        this.selectedTab = tab;
        for (javafx.scene.Node node : tabsContainer.getChildren()) {
            if (node instanceof TabItemView itemView) {
                itemView.setActive(itemView.getTab() == tab);
            }
        }
        if (onTabSelected != null) {
            onTabSelected.accept(tab);
        }
    }

    private void addTabUI(BrowserTab tab) {
        TabItemView itemView = new TabItemView(tab);
        itemView.setOnMouseClicked(e -> selectTab(tab));
        itemView.getCloseButton().setOnAction(e -> {
            e.consume();
            if (onTabClosed != null) onTabClosed.accept(tab);
        });
        tabsContainer.getChildren().add(itemView);
        selectTab(tab);
    }

    private void removeTabUI(BrowserTab tab) {
        tabsContainer.getChildren().removeIf(node -> {
            if (node instanceof TabItemView itemView) {
                return itemView.getTab() == tab;
            }
            return false;
        });
    }

    public void setOnTabSelected(Consumer<BrowserTab> callback) { this.onTabSelected = callback; }
    public void setOnTabClosed(Consumer<BrowserTab> callback) { this.onTabClosed = callback; }
    public void setOnNewTabRequested(Runnable callback) { this.onNewTabRequested = callback; }

    // Inner Tab Item representation
    public static class TabItemView extends HBox {
        private final BrowserTab tab;
        private final Label titleLabel;
        private final Button closeButton;
        private final Circle loadingIndicator;

        public TabItemView(BrowserTab tab) {
            this.tab = tab;
            setAlignment(Pos.CENTER_LEFT);
            setSpacing(8);
            setPadding(new Insets(6, 10, 6, 12));
            setPrefWidth(180);
            setMaxWidth(220);
            getStyleClass().add("tab-item");

            // Small site badge / loading dot
            loadingIndicator = new Circle(4);
            loadingIndicator.getStyleClass().add("tab-dot");

            titleLabel = new Label("New Tab");
            titleLabel.textProperty().bind(tab.titleProperty());
            titleLabel.getStyleClass().add("tab-title");
            titleLabel.setMaxWidth(130);
            HBox.setHgrow(titleLabel, Priority.ALWAYS);

            // Close button
            closeButton = new Button();
            closeButton.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_CLOSE, 10, "#94a3b8"));
            closeButton.getStyleClass().add("tab-close-btn");
            closeButton.setTooltip(new Tooltip("Close Tab (Ctrl+W)"));

            getChildren().addAll(loadingIndicator, titleLabel, closeButton);

            // Tab tooltip
            Tooltip tooltip = new Tooltip();
            tooltip.textProperty().bind(tab.titleProperty());
            Tooltip.install(this, tooltip);

            // Update loading indicator appearance
            tab.loadingProperty().addListener((obs, oldVal, isLoading) -> {
                if (isLoading) {
                    loadingIndicator.setStyle("-fx-fill: #6366f1;");
                } else {
                    loadingIndicator.setStyle("-fx-fill: #3b82f6;");
                }
            });
        }

        public void setActive(boolean active) {
            if (active) {
                if (!getStyleClass().contains("active")) {
                    getStyleClass().add("active");
                }
            } else {
                getStyleClass().remove("active");
            }
        }

        public BrowserTab getTab() { return tab; }
        public Button getCloseButton() { return closeButton; }
    }
}
