package com.browser;

public class Launcher {
    public static void main(String[] args) {
        System.setProperty("com.sun.webkit.useHTTP2Loader", "false");
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv4Addresses", "true");
        BrowserApp.main(args);
    }
}
