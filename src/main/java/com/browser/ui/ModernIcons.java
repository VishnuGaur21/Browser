package com.browser.ui;

import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class ModernIcons {
    // Clean SVG paths (standard Feather / Lucide style)
    public static final String PATH_BACK = "M 15 18 L 9 12 L 15 6";
    public static final String PATH_FORWARD = "M 9 18 L 15 12 L 9 6";
    public static final String PATH_REFRESH = "M 23 4 L 23 10 L 17 10 M 1 20 L 1 14 L 7 14 M 3.51 9 a 9 9 0 0 1 14.85 -3.36 L 23 10 M 1 14 l 4.64 4.36 A 9 9 0 0 0 20.49 15";
    public static final String PATH_CLOSE = "M 18 6 L 6 18 M 6 6 L 18 18";
    public static final String PATH_HOME = "M 3 9 l 9 -7 l 9 7 v 11 a 2 2 0 0 1 -2 2 H 5 a 2 2 0 0 1 -2 -2 Z";
    public static final String PATH_PLUS = "M 12 5 v 14 M 5 12 h 14";
    public static final String PATH_SEARCH = "M 11 19 A 8 8 0 1 0 11 3 a 8 8 0 0 0 0 16 Z M 21 21 l -4.35 -4.35";
    public static final String PATH_STAR = "M 12 2 l 3.09 6.26 L 22 9.27 l -5 4.87 l 1.18 6.88 L 12 17.77 l -6.18 3.25 L 7 14.14 L 2 9.27 l 6.91 -1.01 Z";
    public static final String PATH_LOCK = "M 7 11 V 7 a 5 5 0 0 1 10 0 v 4 M 5 11 h 14 a 2 2 0 0 1 2 2 v 7 a 2 2 0 0 1 -2 2 H 5 a 2 2 0 0 1 -2 -2 v -7 a 2 2 0 0 1 2 -2 Z";
    public static final String PATH_GLOBE = "M 12 2 a 10 10 0 1 0 10 10 A 10 10 0 0 0 12 2 Z M 2 12 h 20 M 12 2 a 15.3 15.3 0 0 1 4 10 a 15.3 15.3 0 0 1 -4 10 a 15.3 15.3 0 0 1 -4 -10 a 15.3 15.3 0 0 1 4 -10 Z";
    public static final String PATH_SPARKLES = "M 12 3 l 2.5 5.5 L 20 11 l -5.5 2.5 L 12 19 l -2.5 -5.5 L 4 11 l 5.5 -2.5 Z M 19 3 l 1 2 l 2 1 l -2 1 l -1 2 l -1 -2 l -2 -1 l 2 -1 Z";
    public static final String PATH_HISTORY = "M 12 8 v 4 l 3 3 M 12 2 a 10 10 0 1 0 10 10 A 10 10 0 0 0 12 2 Z";
    public static final String PATH_DOWNLOAD = "M 21 15 v 4 a 2 2 0 0 1 -2 2 H 5 a 2 2 0 0 1 -2 -2 v -4 M 7 10 l 5 5 l 5 -5 M 12 15 V 3";
    public static final String PATH_SETTINGS = "M 12 15 a 3 3 0 1 0 0 -6 a 3 3 0 0 0 0 6 Z M 19.4 15 a 1.65 1.65 0 0 0 .33 1.82 l .06 .06 a 2 2 0 1 1 -2.83 2.83 l -.06 -.06 a 1.65 1.65 0 0 0 -1.82 -.33 a 1.65 1.65 0 0 0 -1 1.51 V 21 a 2 2 0 0 1 -4 0 v -.09 A 1.65 1.65 0 0 0 9 19.4 a 1.65 1.65 0 0 0 -1.82 .33 l -.06 .06 a 2 2 0 1 1 -2.83 -2.83 l .06 -.06 a 1.65 1.65 0 0 0 .33 -1.82 a 1.65 1.65 0 0 0 -1.51 -1 H 3 a 2 2 0 0 1 0 -4 h .09 A 1.65 1.65 0 0 0 4.6 9 a 1.65 1.65 0 0 0 -.33 -1.82 l -.06 -.06 a 2 2 0 1 1 2.83 -2.83 l .06 .06 a 1.65 1.65 0 0 0 1.82 .33 H 9 a 1.65 1.65 0 0 0 1 -1.51 V 3 a 2 2 0 0 1 4 0 v .09 a 1.65 1.65 0 0 0 1 1.51 a 1.65 1.65 0 0 0 1.82 -.33 l .06 -.06 a 2 2 0 1 1 2.83 2.83 l -.06 .06 a 1.65 1.65 0 0 0 -.33 1.82 V 9 a 1.65 1.65 0 0 0 1.51 1 H 21 a 2 2 0 0 1 0 4 h -.09 a 1.65 1.65 0 0 0 -1.51 1 Z";
    public static final String PATH_CODE = "M 16 18 l 6 -6 l -6 -6 M 8 6 l -6 6 l 6 6";
    public static final String PATH_MENU = "M 3 12 h 18 M 3 6 h 18 M 3 18 h 18";

    public static SVGPath createIcon(String content, double scale, String strokeColor, String fillColor) {
        SVGPath path = new SVGPath();
        path.setContent(content);
        path.setStroke(Color.web(strokeColor));
        path.setStrokeWidth(2.0);
        path.setFill(fillColor != null ? Color.web(fillColor) : Color.TRANSPARENT);
        path.setScaleX(scale);
        path.setScaleY(scale);
        return path;
    }

    public static SVGPath createStrokeIcon(String content, double size, String color) {
        SVGPath path = new SVGPath();
        path.setContent(content);
        path.setStroke(Color.web(color));
        path.setStrokeWidth(2.0);
        path.setFill(Color.TRANSPARENT);
        // Default viewBox is roughly 24x24
        double scale = size / 24.0;
        path.setScaleX(scale);
        path.setScaleY(scale);
        return path;
    }

    public static SVGPath createFilledIcon(String content, double size, String color) {
        SVGPath path = new SVGPath();
        path.setContent(content);
        path.setFill(Color.web(color));
        path.setStroke(Color.web(color));
        path.setStrokeWidth(1.0);
        double scale = size / 24.0;
        path.setScaleX(scale);
        path.setScaleY(scale);
        return path;
    }
}
