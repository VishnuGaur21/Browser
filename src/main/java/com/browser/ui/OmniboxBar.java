package com.browser.ui;

import com.browser.service.BookmarkManager;
import com.browser.service.ConfigManager;
import com.browser.service.SearchSuggestionService;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.util.List;
import java.util.function.Consumer;

public class OmniboxBar extends HBox {
    private final BookmarkManager bookmarkManager;
    private final ConfigManager configManager;
    private final SearchSuggestionService suggestionService;

    // Controls
    private final Button backBtn;
    private final Button forwardBtn;
    private final Button refreshBtn;
    private final Button homeBtn;

    private final StackPane omniboxContainer;
    private final Label securityBadge;
    private final TextField addressField;
    private final ProgressBar progressBar;
    private final Button bookmarkBtn;
    private final Button zoomBtn;
    private final Button readerBtn;

    private final Button historyBtn;
    private final Button downloadsBtn;
    private final Button aiCopilotBtn;
    private final Button menuBtn;

    private final ContextMenu suggestionPopup;

    private BrowserTab activeTab;
    private boolean isTyping = false;

    // Callbacks
    private Consumer<String> onNavigate;
    private Runnable onToggleAiCopilot;
    private Runnable onOpenHistory;
    private Runnable onOpenDownloads;
    private Runnable onOpenSettings;
    private Runnable onOpenPageSource;
    private Runnable onToggleFindInPage;
    private Runnable onToggleBookmarksBar;

    public OmniboxBar(BookmarkManager bookmarkManager, ConfigManager configManager) {
        this.bookmarkManager = bookmarkManager;
        this.configManager = configManager;
        this.suggestionService = new SearchSuggestionService();
        this.suggestionPopup = new ContextMenu();
        this.suggestionPopup.getStyleClass().add("suggestion-popup");
        this.suggestionPopup.setAutoHide(true);

        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(6, 14, 8, 14));
        setSpacing(8);
        getStyleClass().add("omnibox-bar-root");

        // Nav Buttons
        backBtn = createIconButton(ModernIcons.PATH_BACK, "Back (Alt+Left)");
        forwardBtn = createIconButton(ModernIcons.PATH_FORWARD, "Forward (Alt+Right)");
        refreshBtn = createIconButton(ModernIcons.PATH_REFRESH, "Reload (Ctrl+R / F5)");
        homeBtn = createIconButton(ModernIcons.PATH_HOME, "Home (Alt+Home)");

        // Omnibox Center Container
        omniboxContainer = new StackPane();
        omniboxContainer.getStyleClass().add("omnibox-container");
        HBox.setHgrow(omniboxContainer, Priority.ALWAYS);

        // Security indicator
        securityBadge = new Label();
        securityBadge.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_LOCK, 13, "#10b981"));
        securityBadge.getStyleClass().add("security-badge");

        // Address Field
        addressField = new TextField();
        addressField.setPromptText("Search with Google or enter web address...");
        addressField.getStyleClass().add("omnibox-field");

        // In-Omnibox Right Actions
        bookmarkBtn = new Button();
        bookmarkBtn.getStyleClass().add("omnibox-action-btn");
        bookmarkBtn.setTooltip(new Tooltip("Bookmark this tab (Ctrl+D)"));
        updateBookmarkIcon(false);

        zoomBtn = new Button("100%");
        zoomBtn.getStyleClass().add("omnibox-zoom-btn");
        zoomBtn.setTooltip(new Tooltip("Zoom (Click to reset)"));

        readerBtn = new Button();
        readerBtn.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_CODE, 13, "#94a3b8"));
        readerBtn.getStyleClass().add("omnibox-action-btn");
        readerBtn.setTooltip(new Tooltip("Toggle Reader Mode"));

        HBox omniboxInner = new HBox(6);
        omniboxInner.setAlignment(Pos.CENTER_LEFT);
        omniboxInner.setPadding(new Insets(2, 6, 2, 8));
        HBox.setHgrow(addressField, Priority.ALWAYS);

        omniboxInner.getChildren().addAll(securityBadge, addressField, readerBtn, zoomBtn, bookmarkBtn);

        // Progress bar anchored at bottom
        progressBar = new ProgressBar(0);
        progressBar.setMaxHeight(2.5);
        progressBar.setPrefHeight(2.5);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.getStyleClass().add("omnibox-progress-bar");
        progressBar.setVisible(false);
        StackPane.setAlignment(progressBar, Pos.BOTTOM_CENTER);

        omniboxContainer.getChildren().addAll(omniboxInner, progressBar);

        // Right side tools
        historyBtn = createIconButton(ModernIcons.PATH_HISTORY, "Browsing History (Ctrl+H)");
        downloadsBtn = createIconButton(ModernIcons.PATH_DOWNLOAD, "Downloads (Ctrl+J)");

        aiCopilotBtn = new Button("AI Copilot");
        aiCopilotBtn.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_SPARKLES, 15, "#a855f7"));
        aiCopilotBtn.getStyleClass().add("ai-copilot-btn");
        aiCopilotBtn.setTooltip(new Tooltip("Autonomous AI Copilot (Ctrl+I)"));

        menuBtn = createIconButton(ModernIcons.PATH_MENU, "Browser Tools & Settings");

        getChildren().addAll(backBtn, forwardBtn, refreshBtn, homeBtn, omniboxContainer,
                historyBtn, downloadsBtn, aiCopilotBtn, menuBtn);

        setupEventHandlers();
    }

    private Button createIconButton(String svgPath, String tooltipText) {
        Button btn = new Button();
        btn.setGraphic(ModernIcons.createStrokeIcon(svgPath, 14, "#94a3b8"));
        btn.getStyleClass().add("nav-icon-btn");
        btn.setTooltip(new Tooltip(tooltipText));
        return btn;
    }

    private void setupEventHandlers() {
        backBtn.setOnAction(e -> { if (activeTab != null) activeTab.goBack(); });
        forwardBtn.setOnAction(e -> { if (activeTab != null) activeTab.goForward(); });
        refreshBtn.setOnAction(e -> {
            if (activeTab != null) {
                if (activeTab.loadingProperty().get()) activeTab.stop();
                else activeTab.reload();
            }
        });
        homeBtn.setOnAction(e -> { if (activeTab != null) activeTab.loadStartPage(); });

        zoomBtn.setOnAction(e -> { if (activeTab != null) activeTab.resetZoom(); });
        readerBtn.setOnAction(e -> { if (activeTab != null) activeTab.toggleReaderMode(); });

        bookmarkBtn.setOnAction(e -> {
            if (activeTab != null) {
                String currentUrl = activeTab.urlProperty().get();
                String currentTitle = activeTab.titleProperty().get();
                bookmarkManager.toggleBookmark(currentTitle, currentUrl);
                updateBookmarkIcon(bookmarkManager.isBookmarked(currentUrl));
            }
        });

        historyBtn.setOnAction(e -> { if (onOpenHistory != null) onOpenHistory.run(); });
        downloadsBtn.setOnAction(e -> { if (onOpenDownloads != null) onOpenDownloads.run(); });
        aiCopilotBtn.setOnAction(e -> { if (onToggleAiCopilot != null) onToggleAiCopilot.run(); });

        setupMenuButton();

        // Address Field Interactions
        addressField.setOnAction(e -> {
            suggestionPopup.hide();
            String query = addressField.getText().trim();
            if (onNavigate != null && !query.isBlank()) {
                onNavigate.accept(query);
            }
        });

        addressField.focusedProperty().addListener((obs, oldVal, isFocused) -> {
            if (isFocused) {
                Platform.runLater(addressField::selectAll);
            } else {
                suggestionPopup.hide();
            }
        });

        // Search suggestions as user types
        addressField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!addressField.isFocused()) return;
            if (newVal == null || newVal.isBlank()) {
                suggestionPopup.hide();
                return;
            }

            suggestionService.fetchSuggestions(newVal, list -> {
                if (!addressField.isFocused() || list.isEmpty()) {
                    suggestionPopup.hide();
                    return;
                }
                showSuggestions(list);
            });
        });

        addressField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                suggestionPopup.hide();
            }
        });
    }

    private void showSuggestions(List<String> suggestions) {
        suggestionPopup.getItems().clear();
        for (String item : suggestions) {
            MenuItem mi = new MenuItem(item);
            mi.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_SEARCH, 12, "#64748b"));
            mi.setOnAction(e -> {
                addressField.setText(item);
                if (onNavigate != null) onNavigate.accept(item);
            });
            suggestionPopup.getItems().add(mi);
        }
        if (!suggestionPopup.isShowing() && addressField.getScene() != null) {
            suggestionPopup.show(addressField, Side.BOTTOM, 0, 4);
        }
    }

    private void setupMenuButton() {
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("tools-context-menu");

        MenuItem findItem = new MenuItem("Find in Page... (Ctrl+F)");
        findItem.setOnAction(e -> { if (onToggleFindInPage != null) onToggleFindInPage.run(); });

        MenuItem bookmarksBarItem = new MenuItem("Toggle Bookmarks Bar (Ctrl+Shift+B)");
        bookmarksBarItem.setOnAction(e -> { if (onToggleBookmarksBar != null) onToggleBookmarksBar.run(); });

        MenuItem pageSourceItem = new MenuItem("View Page Source (Ctrl+U)");
        pageSourceItem.setOnAction(e -> { if (onOpenPageSource != null) onOpenPageSource.run(); });

        MenuItem zoomInItem = new MenuItem("Zoom In (+10%)");
        zoomInItem.setOnAction(e -> { if (activeTab != null) activeTab.zoomIn(); });

        MenuItem zoomOutItem = new MenuItem("Zoom Out (-10%)");
        zoomOutItem.setOnAction(e -> { if (activeTab != null) activeTab.zoomOut(); });

        MenuItem settingsItem = new MenuItem("Browser Settings");
        settingsItem.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_SETTINGS, 13, "#94a3b8"));
        settingsItem.setOnAction(e -> { if (onOpenSettings != null) onOpenSettings.run(); });

        menu.getItems().addAll(findItem, bookmarksBarItem, new SeparatorMenuItem(),
                zoomInItem, zoomOutItem, new SeparatorMenuItem(),
                pageSourceItem, new SeparatorMenuItem(), settingsItem);

        menuBtn.setOnMouseClicked(e -> menu.show(menuBtn, Side.BOTTOM, -120, 4));
    }

    public void bindTab(BrowserTab tab) {
        this.activeTab = tab;
        if (tab == null) return;

        // Bind Nav Buttons
        backBtn.disableProperty().bind(tab.canGoBackProperty().not());
        forwardBtn.disableProperty().bind(tab.canGoForwardProperty().not());

        // Bind reload/stop icon
        tab.loadingProperty().addListener((obs, oldVal, isLoading) -> {
            if (isLoading) {
                refreshBtn.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_CLOSE, 14, "#f87171"));
                refreshBtn.getTooltip().setText("Stop loading (Esc)");
            } else {
                refreshBtn.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_REFRESH, 14, "#94a3b8"));
                refreshBtn.getTooltip().setText("Reload (Ctrl+R / F5)");
            }
        });

        // Bind progress bar
        progressBar.progressProperty().bind(tab.progressProperty());
        tab.loadingProperty().addListener((obs, oldVal, isLoading) -> progressBar.setVisible(isLoading));

        // Bind zoom button text
        tab.zoomProperty().addListener((obs, oldVal, newVal) -> {
            int pct = (int) Math.round(newVal.doubleValue() * 100);
            zoomBtn.setText(pct + "%");
        });

        // Update address field when URL changes
        tab.urlProperty().addListener((obs, oldVal, newUrl) -> {
            if (!addressField.isFocused()) {
                updateUrlDisplay(newUrl);
            }
            updateBookmarkIcon(bookmarkManager.isBookmarked(newUrl));
        });

        updateUrlDisplay(tab.urlProperty().get());
        updateBookmarkIcon(bookmarkManager.isBookmarked(tab.urlProperty().get()));
    }

    private void updateUrlDisplay(String fullUrl) {
        if (fullUrl == null || fullUrl.isBlank() || fullUrl.contains("startpage.html")) {
            addressField.setText("");
            securityBadge.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_HOME, 13, "#6366f1"));
        } else if (fullUrl.startsWith("https://")) {
            addressField.setText(fullUrl);
            securityBadge.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_LOCK, 13, "#10b981"));
        } else if (fullUrl.startsWith("http://")) {
            addressField.setText(fullUrl);
            securityBadge.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_GLOBE, 13, "#f59e0b"));
        } else {
            addressField.setText(fullUrl);
            securityBadge.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_GLOBE, 13, "#94a3b8"));
        }
    }

    public void updateBookmarkIcon(boolean isBookmarked) {
        if (isBookmarked) {
            bookmarkBtn.setGraphic(ModernIcons.createFilledIcon(ModernIcons.PATH_STAR, 14, "#fbbf24"));
            bookmarkBtn.getTooltip().setText("Remove bookmark");
        } else {
            bookmarkBtn.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_STAR, 14, "#94a3b8"));
            bookmarkBtn.getTooltip().setText("Bookmark this page");
        }
    }

    // Callbacks setter
    public void setOnNavigate(Consumer<String> onNavigate) { this.onNavigate = onNavigate; }
    public void setOnToggleAiCopilot(Runnable onToggleAiCopilot) { this.onToggleAiCopilot = onToggleAiCopilot; }
    public void setOnOpenHistory(Runnable onOpenHistory) { this.onOpenHistory = onOpenHistory; }
    public void setOnOpenDownloads(Runnable onOpenDownloads) { this.onOpenDownloads = onOpenDownloads; }
    public void setOnOpenSettings(Runnable onOpenSettings) { this.onOpenSettings = onOpenSettings; }
    public void setOnOpenPageSource(Runnable onOpenPageSource) { this.onOpenPageSource = onOpenPageSource; }
    public void setOnToggleFindInPage(Runnable onToggleFindInPage) { this.onToggleFindInPage = onToggleFindInPage; }
    public void setOnToggleBookmarksBar(Runnable onToggleBookmarksBar) { this.onToggleBookmarksBar = onToggleBookmarksBar; }
    public TextField getAddressField() { return addressField; }
}
