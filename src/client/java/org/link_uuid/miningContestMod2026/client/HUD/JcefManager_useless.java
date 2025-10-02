package org.link_uuid.miningContestMod2026.client.HUD;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;

public class JcefManager_useless {
    private static CefApp cefApp;
    private static CefClient client;
    private static CefBrowser browser;

    public static void init() {
        if (cefApp != null) return;

        CefSettings settings = new CefSettings();
        settings.windowless_rendering_enabled = true;
        cefApp = CefApp.getInstance(settings);
        client = cefApp.createClient();

        // 加载本地 HTTP 页面
        browser = client.createBrowser("http://localhost:8080/phone.html", true, false);
    }

    public static CefBrowser getBrowser() {
        return browser;
    }
}
