package com.browser.ui;

import com.browser.model.BookmarkItem;
import com.browser.service.BookmarkManager;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BookmarksBar extends HBox {
    private final BookmarkManager bookmarkManager;
    private final HBox itemsBox;

    private Consumer<String> onNavigate;
    private Consumer<String> onOpenInNewTab;

    public BookmarksBar(BookmarkManager bookmarkManager) {
        this.bookmarkManager = bookmarkManager;

        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(2, 12, 4, 12));
        setSpacing(6);
        getStyleClass().add("bookmarks-bar-root");

        itemsBox = new HBox(6);
        itemsBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(itemsBox, Priority.ALWAYS);

        // Add Bookmark button
        Button addBtn = new Button();
        addBtn.setGraphic(ModernIcons.createStrokeIcon(ModernIcons.PATH_PLUS, 11, "#94a3b8"));
        addBtn.getStyleClass().add("bookmark-pill-btn");
        addBtn.setTooltip(new Tooltip("Add current page to bookmarks"));
        addBtn.setOnAction(e -> {
            // Handled via main window or popup
        });

        getChildren().addAll(itemsBox);

        // Populate initial bookmarks
        rebuildBookmarks();

        bookmarkManager.getBookmarks().addListener((ListChangeListener<BookmarkItem>) c -> rebuildBookmarks());
    }

    private void rebuildBookmarks() {
        itemsBox.getChildren().clear();
        for (BookmarkItem item : bookmarkManager.getBookmarks()) {
            Button btn = new Button(item.getTitle());
            btn.setGraphic(ModernIcons.createFilledIcon(ModernIcons.PATH_STAR, 11, "#e2e8f0"));
            btn.getStyleClass().add("bookmark-pill-btn");
            btn.setTooltip(new Tooltip(item.getTitle() + "\n" + item.getUrl()));

            btn.setOnAction(e -> {
                if (onNavigate != null) onNavigate.accept(item.getUrl());
            });

            // Context menu for bookmarks
            ContextMenu contextMenu = new ContextMenu();
            MenuItem openItem = new MenuItem("Open");
            openItem.setOnAction(e -> { if (onNavigate != null) onNavigate.accept(item.getUrl()); });

            MenuItem openNewTabItem = new MenuItem("Open in New Tab");
            openNewTabItem.setOnAction(e -> { if (onOpenInNewTab != null) onOpenInNewTab.accept(item.getUrl()); });

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(e -> bookmarkManager.removeBookmark(item.getUrl()));

            contextMenu.getItems().addAll(openItem, openNewTabItem, deleteItem);
            btn.setContextMenu(contextMenu);

            itemsBox.getChildren().add(btn);
        }
    }

    public void setOnNavigate(Consumer<String> onNavigate) { this.onNavigate = onNavigate; }
    public void setOnOpenInNewTab(Consumer<String> onOpenInNewTab) { this.onOpenInNewTab = onOpenInNewTab; }
}
