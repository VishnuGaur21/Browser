package com.browser.ui;

import com.browser.service.GeminiService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import org.json.JSONObject;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GeminiSidebar extends VBox {
    private final GeminiService geminiService;
    private final Supplier<BrowserTab> activeTabSupplier;
    private final Consumer<JSONObject> onBrowserAction;
    private final Runnable onClose;

    private final VBox messagesBox;
    private final ScrollPane scrollPane;
    private final TextField inputField;
    private final Button sendBtn;
    private final ProgressIndicator loadingIndicator;

    public GeminiSidebar(GeminiService geminiService,
                         Supplier<BrowserTab> activeTabSupplier,
                         Consumer<JSONObject> onBrowserAction,
                         Runnable onClose) {
        this.geminiService = geminiService;
        this.activeTabSupplier = activeTabSupplier;
        this.onBrowserAction = onBrowserAction;
        this.onClose = onClose;

        setPrefWidth(360);
        setMinWidth(320);
        setMaxWidth(440);
        getStyleClass().add("ai-sidebar-root");
        setSpacing(10);
        setPadding(new Insets(12, 12, 14, 12));

        // Top Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Autonomous Copilot");
        title.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_SPARKLES, 16, "#a855f7"));
        title.getStyleClass().add("ai-header-title");
        HBox.setHgrow(title, Priority.ALWAYS);

        Label statusBadge = new Label("Full Task Automation");
        statusBadge.getStyleClass().add("ai-status-badge");

        Button closeBtn = new Button();
        closeBtn.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_CLOSE, 12, "#94a3b8"));
        closeBtn.getStyleClass().add("ai-close-btn");
        closeBtn.setOnAction(e -> { if (onClose != null) onClose.run(); });

        header.getChildren().addAll(title, statusBadge, closeBtn);

        // Quick Action Chips Row
        FlowPane chipsPane = new FlowPane(6, 6);
        chipsPane.setAlignment(Pos.CENTER_LEFT);

        Button playChip = createChip("▶ Play Music", () -> triggerPrompt("open youtube and play music"));
        Button ytChip = createChip("📺 YouTube", () -> triggerPrompt("open youtube"));
        Button searchChip = createChip("🔍 Search News", () -> triggerPrompt("search latest tech news"));
        Button summarizeChip = createChip("📄 Summarize", () -> triggerPrompt("Summarize this webpage concisely with bullet points and main takeaways."));
        Button codeChip = createChip("💻 Java Code", () -> triggerPrompt("Show me a thread-safe Singleton pattern in Java."));

        chipsPane.getChildren().addAll(playChip, ytChip, searchChip, summarizeChip, codeChip);

        // Messages Box inside ScrollPane
        messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(4, 4, 4, 4));

        scrollPane = new ScrollPane(messagesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("ai-scroll-pane");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Welcome Greeting
        addBotMessage("👋 **Hello! I'm your Autonomous AI Copilot.**\n\nI can execute browser actions for you with **full permission**!\n\n• **Autonomous Browser Tasks:** Say *\"open youtube and play music\"*, *\"search for quantum computing\"*, *\"open github\"*, or *\"close tab\"* and I will automatically control the browser to execute it!\n• **Web Analysis & Summaries:** Summarize or extract insights from any open webpage.\n• **Code & Problem Solving:** Java, Python, Web dev, architecture & math.\n\nTry clicking **▶ Play Music** or type any task below!", false);

        // Loading Indicator (hidden by default)
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(22, 22);
        loadingIndicator.setVisible(false);

        // Bottom Input Row
        HBox inputRow = new HBox(6);
        inputRow.setAlignment(Pos.CENTER);

        inputField = new TextField();
        inputField.setPromptText("Ask anything or give a task (e.g. 'play music on youtube')...");
        inputField.getStyleClass().add("ai-input-field");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        sendBtn = new Button();
        sendBtn.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_FORWARD, 14, "#ffffff"));
        sendBtn.getStyleClass().add("ai-send-btn");

        inputRow.getChildren().addAll(inputField, loadingIndicator, sendBtn);

        getChildren().addAll(header, chipsPane, scrollPane, inputRow);

        // Actions
        sendBtn.setOnAction(e -> handleSend());
        inputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleSend();
            }
        });
    }

    private Button createChip(String text, Runnable action) {
        Button chip = new Button(text);
        chip.getStyleClass().add("ai-quick-chip");
        chip.setOnAction(e -> action.run());
        return chip;
    }

    private void handleSend() {
        String query = inputField.getText().trim();
        if (query.isBlank()) return;
        inputField.clear();
        triggerPrompt(query);
    }

    public void triggerPrompt(String prompt) {
        addUserMessage(prompt);
        loadingIndicator.setVisible(true);

        BrowserTab tab = activeTabSupplier.get();
        if (tab != null) {
            String title = tab.titleProperty().get();
            String url = tab.urlProperty().get();

            tab.extractPageText(pageText -> {
                geminiService.ask(prompt, title, url, pageText,
                        (response, action) -> {
                            loadingIndicator.setVisible(false);
                            addBotMessage(response, action != null);
                            if (action != null && onBrowserAction != null) {
                                onBrowserAction.accept(action);
                            }
                        },
                        error -> {
                            loadingIndicator.setVisible(false);
                            addBotMessage("⚠️ Error: " + error, false);
                        });
            });
        } else {
            geminiService.ask(prompt, "", "", "",
                    (response, action) -> {
                        loadingIndicator.setVisible(false);
                        addBotMessage(response, action != null);
                        if (action != null && onBrowserAction != null) {
                            onBrowserAction.accept(action);
                        }
                    },
                    error -> {
                        loadingIndicator.setVisible(false);
                        addBotMessage("⚠️ Error: " + error, false);
                    });
        }
    }

    private void addUserMessage(String text) {
        VBox card = new VBox(4);
        card.getStyleClass().add("ai-user-bubble");
        card.setAlignment(Pos.CENTER_RIGHT);

        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add("ai-user-text");

        card.getChildren().add(label);
        messagesBox.getChildren().add(card);
        scrollToBottom();
    }

    private void addBotMessage(String text) {
        addBotMessage(text, false);
    }

    private void addBotMessage(String text, boolean isAction) {
        VBox card = new VBox(6);
        card.getStyleClass().add("ai-bot-bubble");

        HBox senderBox = new HBox(6);
        senderBox.setAlignment(Pos.CENTER_LEFT);

        Label sender = new Label("AI Autonomous Copilot");
        sender.getStyleClass().add("ai-bot-name");
        sender.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_SPARKLES, 12, "#a855f7"));
        senderBox.getChildren().add(sender);

        if (isAction) {
            Label badge = new Label("⚡ Task Executed");
            badge.setStyle("-fx-background-color: rgba(34, 197, 94, 0.2); -fx-text-fill: #4ade80; -fx-font-size: 10px; -fx-font-weight: 700; -fx-padding: 2 6 2 6; -fx-background-radius: 4;");
            senderBox.getChildren().add(badge);
        }

        Label content = new Label(text);
        content.setWrapText(true);
        content.getStyleClass().add("ai-bot-text");

        card.getChildren().addAll(senderBox, content);
        messagesBox.getChildren().add(card);
        scrollToBottom();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }
}

