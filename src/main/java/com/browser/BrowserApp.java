package com.browser;

import com.browser.model.HistoryItem;
import com.browser.service.*;
import com.browser.ui.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.File;

public class BrowserApp extends Application {
    private Stage primaryStage;

    // Services
    private ConfigManager configManager;
    private BookmarkManager bookmarkManager;
    private HistoryManager historyManager;
    private GeminiService geminiService;
    private DownloadService downloadService;

    // UI Components
    private BorderPane rootLayout;
    private VBox topContainer;
    private TabBarView tabBarView;
    private OmniboxBar omniboxBar;
    private BookmarksBar bookmarksBar;
    private StackPane centerStack;
    private GeminiSidebar geminiSidebar;
    private FindInPageBar findInPageBar;
    private HBox statusBar;
    private Label statusLabel;
    private Label zoomStatusLabel;

    // Tabs
    private final ObservableList<BrowserTab> tabs = FXCollections.observableArrayList();
    private BrowserTab activeTab;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // Initialize Services
        configManager = new ConfigManager();
        bookmarkManager = new BookmarkManager();
        historyManager = new HistoryManager();
        geminiService = new GeminiService(configManager);
        downloadService = new DownloadService();

        // Main Layout
        rootLayout = new BorderPane();
        rootLayout.getStyleClass().add("root-layout");

        // Center Container
        centerStack = new StackPane();
        rootLayout.setCenter(centerStack);

        // Top UI: TabBar + Omnibox + BookmarksBar
        topContainer = new VBox();
        topContainer.getStyleClass().add("window-top-container");

        tabBarView = new TabBarView(tabs);
        omniboxBar = new OmniboxBar(bookmarkManager, configManager);
        bookmarksBar = new BookmarksBar(bookmarkManager);

        topContainer.getChildren().addAll(tabBarView, omniboxBar, bookmarksBar);
        rootLayout.setTop(topContainer);

        // Gemini Sidebar
        geminiSidebar = new GeminiSidebar(geminiService, () -> activeTab, this::executeAiAction, () -> toggleGeminiSidebar());
        geminiSidebar.setVisible(true);
        rootLayout.setRight(geminiSidebar);

        // Find In Page Bar (Floats on center stack top-right)
        findInPageBar = new FindInPageBar(() -> activeTab, () -> findInPageBar.setVisible(false));
        findInPageBar.setVisible(false);
        StackPane.setAlignment(findInPageBar, Pos.TOP_RIGHT);
        StackPane.setMargin(findInPageBar, new Insets(10, 20, 0, 0));

        // Bottom Status Bar
        setupStatusBar();
        rootLayout.setBottom(statusBar);

        // Wire Interactions
        setupActions();

        // Create initial tab
        createNewTab(configManager.getHomePage());

        // Create Scene
        Scene scene = new Scene(rootLayout, 1380, 880);
        try {
            scene.getStylesheets().add(getClass().getResource("/com/browser/styles/browser.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load browser.css: " + e.getMessage());
        }

        setupKeyboardShortcuts(scene);

        stage.setScene(scene);
        stage.setTitle("Modern Browser");
        stage.setMinWidth(960);
        stage.setMinHeight(600);

        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        stage.show();
    }

    private void setupStatusBar() {
        statusBar = new HBox(12);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.getStyleClass().add("status-bar-root");

        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-label");
        HBox.setHgrow(statusLabel, Priority.ALWAYS);

        zoomStatusLabel = new Label("100%");
        zoomStatusLabel.getStyleClass().add("status-label");

        statusBar.getChildren().addAll(statusLabel, zoomStatusLabel);
    }

    private void setupActions() {
        // TabBar callbacks
        tabBarView.setOnTabSelected(this::selectTab);
        tabBarView.setOnTabClosed(this::closeTab);
        tabBarView.setOnNewTabRequested(() -> createNewTab(configManager.getHomePage()));

        // Omnibox callbacks
        omniboxBar.setOnNavigate(target -> {
            if (activeTab != null) activeTab.load(target);
        });
        omniboxBar.setOnToggleAiCopilot(this::toggleGeminiSidebar);
        omniboxBar.setOnOpenHistory(() -> HistoryDialog.show(primaryStage, historyManager, url -> {
            if (activeTab != null) activeTab.load(url);
        }));
        omniboxBar.setOnOpenDownloads(() -> DownloadsDialog.show(primaryStage, downloadService));
        omniboxBar.setOnOpenSettings(() -> SettingsDialog.show(primaryStage, configManager, historyManager, bookmarkManager));
        omniboxBar.setOnOpenPageSource(() -> {
            if (activeTab != null) {
                activeTab.extractPageHtml(html -> {
                    PageSourceDialog.show(primaryStage, activeTab.titleProperty().get(), html);
                });
            }
        });
        omniboxBar.setOnToggleFindInPage(this::toggleFindInPage);
        omniboxBar.setOnToggleBookmarksBar(this::toggleBookmarksBar);

        // BookmarksBar callbacks
        bookmarksBar.setOnNavigate(url -> {
            if (activeTab != null) activeTab.load(url);
        });
        bookmarksBar.setOnOpenInNewTab(url -> createNewTab(url));
    }

    public BrowserTab createNewTab(String urlToLoad) {
        BrowserTab tab = new BrowserTab(historyManager, configManager);
        tabs.add(tab);
        tabBarView.selectTab(tab);

        if (urlToLoad != null && !urlToLoad.isBlank()) {
            tab.load(urlToLoad);
        } else {
            tab.loadStartPage();
        }
        return tab;
    }

    public void selectTab(BrowserTab tab) {
        if (tab == null) return;
        this.activeTab = tab;

        centerStack.getChildren().clear();
        centerStack.getChildren().addAll(tab, findInPageBar);

        omniboxBar.bindTab(tab);

        // Bind status bar
        tab.loadingProperty().addListener((obs, oldVal, isLoading) -> {
            if (isLoading) {
                statusLabel.setText("Loading " + tab.urlProperty().get() + "...");
            } else {
                statusLabel.setText("Done");
            }
        });

        tab.zoomProperty().addListener((obs, oldVal, newVal) -> {
            int pct = (int) Math.round(newVal.doubleValue() * 100);
            zoomStatusLabel.setText(pct + "%");
        });
    }

    public void closeTab(BrowserTab tab) {
        int index = tabs.indexOf(tab);
        tabs.remove(tab);

        if (tabs.isEmpty()) {
            createNewTab(configManager.getHomePage());
        } else {
            int newIndex = Math.min(index, tabs.size() - 1);
            tabBarView.selectTab(tabs.get(newIndex));
        }
    }

    public void toggleGeminiSidebar() {
        boolean isVisible = geminiSidebar.isVisible();
        geminiSidebar.setVisible(!isVisible);
        geminiSidebar.setManaged(!isVisible);
        if (!isVisible) {
            rootLayout.setRight(geminiSidebar);
        } else {
            rootLayout.setRight(null);
        }
    }

    public void toggleBookmarksBar() {
        boolean isVisible = bookmarksBar.isVisible();
        bookmarksBar.setVisible(!isVisible);
        bookmarksBar.setManaged(!isVisible);
    }

    public void toggleFindInPage() {
        boolean isVisible = findInPageBar.isVisible();
        findInPageBar.setVisible(!isVisible);
        if (!isVisible) {
            findInPageBar.focusAndSelect();
        }
    }

    public void executeAiAction(JSONObject action) {
        if (action == null) return;
        Platform.runLater(() -> {
            try {
                String type = action.optString("type", "");
                String target = action.optString("target", "");
                String query = action.optString("query", "");

                switch (type) {
                    case "OPEN_CHROME":
                        try {
                            String chromeUrl = !target.isBlank() ? target :
                                    (!query.isBlank() ? "https://www.youtube.com/results?search_query=" + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8) : "https://www.google.com");
                            new ProcessBuilder("cmd", "/c", "start", "chrome", chromeUrl).start();
                            statusLabel.setText("⚡ AI Action: Launched Google Chrome with " + chromeUrl);
                        } catch (Exception e) {
                            System.err.println("Could not launch Chrome: " + e.getMessage());
                        }
                        break;
                    case "PLAY_YOUTUBE":
                        if (activeTab != null) {
                            activeTab.playYouTubeAndAutoStart(query, target);
                            statusLabel.setText("⚡ AI Action: Playing on YouTube - " + (query != null ? query : ""));
                        }
                        break;
                    case "NAVIGATE":
                        if (activeTab != null && !target.isBlank()) {
                            activeTab.load(target);
                            statusLabel.setText("⚡ AI Action: Navigated to " + target);
                        }
                        break;
                    case "SEARCH":
                        if (activeTab != null && !query.isBlank()) {
                            String searchUrl = configManager.getSearchUrl(query);
                            activeTab.load(searchUrl);
                            statusLabel.setText("⚡ AI Action: Searching Google for \"" + query + "\"");
                        }
                        break;
                    case "NEW_TAB":
                        String tabUrl = !target.isBlank() ? target : configManager.getHomePage();
                        createNewTab(tabUrl);
                        statusLabel.setText("⚡ AI Action: New tab opened");
                        break;
                    case "CLOSE_TAB":
                        if (activeTab != null) {
                            closeTab(activeTab);
                            statusLabel.setText("⚡ AI Action: Tab closed");
                        }
                        break;
                    case "BOOKMARK":
                        if (activeTab != null) {
                            bookmarkManager.toggleBookmark(activeTab.titleProperty().get(), activeTab.urlProperty().get());
                            omniboxBar.updateBookmarkIcon(bookmarkManager.isBookmarked(activeTab.urlProperty().get()));
                            statusLabel.setText("⚡ AI Action: Bookmarked page");
                        }
                        break;
                    case "ZOOM_IN":
                        if (activeTab != null) {
                            activeTab.zoomIn();
                            statusLabel.setText("⚡ AI Action: Zoom in");
                        }
                        break;
                    case "ZOOM_OUT":
                        if (activeTab != null) {
                            activeTab.zoomOut();
                            statusLabel.setText("⚡ AI Action: Zoom out");
                        }
                        break;
                    case "RELOAD":
                        if (activeTab != null) {
                            activeTab.reload();
                            statusLabel.setText("⚡ AI Action: Reloading page");
                        }
                        break;
                    case "READER_MODE":
                        if (activeTab != null) {
                            activeTab.toggleReaderMode();
                            statusLabel.setText("⚡ AI Action: Toggled Reader Mode");
                        }
                        break;
                    default:
                        System.out.println("Unknown AI Action: " + type);
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error executing AI action: " + e.getMessage());
            }
        });
    }

    private void setupKeyboardShortcuts(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() || event.isMetaDown()) {
                if (event.getCode() == KeyCode.T) {
                    event.consume();
                    createNewTab(configManager.getHomePage());
                } else if (event.getCode() == KeyCode.W) {
                    event.consume();
                    if (activeTab != null) closeTab(activeTab);
                } else if (event.getCode() == KeyCode.R) {
                    event.consume();
                    if (activeTab != null) activeTab.reload();
                } else if (event.getCode() == KeyCode.L) {
                    event.consume();
                    omniboxBar.getAddressField().requestFocus();
                    omniboxBar.getAddressField().selectAll();
                } else if (event.getCode() == KeyCode.D) {
                    event.consume();
                    if (activeTab != null) {
                        bookmarkManager.toggleBookmark(activeTab.titleProperty().get(), activeTab.urlProperty().get());
                        omniboxBar.updateBookmarkIcon(bookmarkManager.isBookmarked(activeTab.urlProperty().get()));
                    }
                } else if (event.getCode() == KeyCode.H) {
                    event.consume();
                    HistoryDialog.show(primaryStage, historyManager, url -> {
                        if (activeTab != null) activeTab.load(url);
                    });
                } else if (event.getCode() == KeyCode.J) {
                    event.consume();
                    DownloadsDialog.show(primaryStage, downloadService);
                } else if (event.getCode() == KeyCode.F) {
                    event.consume();
                    toggleFindInPage();
                } else if (event.getCode() == KeyCode.I) {
                    event.consume();
                    toggleGeminiSidebar();
                } else if (event.getCode() == KeyCode.U) {
                    event.consume();
                    if (activeTab != null) {
                        activeTab.extractPageHtml(html -> PageSourceDialog.show(primaryStage, activeTab.titleProperty().get(), html));
                    }
                } else if (event.getCode() == KeyCode.EQUALS || event.getCode() == KeyCode.PLUS) {
                    event.consume();
                    if (activeTab != null) activeTab.zoomIn();
                } else if (event.getCode() == KeyCode.MINUS) {
                    event.consume();
                    if (activeTab != null) activeTab.zoomOut();
                } else if (event.getCode() == KeyCode.DIGIT0 || event.getCode() == KeyCode.NUMPAD0) {
                    event.consume();
                    if (activeTab != null) activeTab.resetZoom();
                } else if (event.isShiftDown() && event.getCode() == KeyCode.B) {
                    event.consume();
                    toggleBookmarksBar();
                }
            } else if (event.getCode() == KeyCode.F5) {
                event.consume();
                if (activeTab != null) activeTab.reload();
            } else if (event.isAltDown()) {
                if (event.getCode() == KeyCode.LEFT) {
                    event.consume();
                    if (activeTab != null) activeTab.goBack();
                } else if (event.getCode() == KeyCode.RIGHT) {
                    event.consume();
                    if (activeTab != null) activeTab.goForward();
                } else if (event.getCode() == KeyCode.HOME) {
                    event.consume();
                    if (activeTab != null) activeTab.loadStartPage();
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
