package com.independent.browser;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.*;
import android.widget.*;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private final int BG = Color.rgb(9, 13, 22);
    private final int PANEL = Color.rgb(18, 25, 39);
    private final int BLUE = Color.rgb(91, 151, 255);

    private LinearLayout root, toolbar;
    private EditText address;
    private TextView title, tabCount;
    private FrameLayout webContainer;

    private final List<BrowserTab> tabs = new ArrayList<>();
    private int activeTab = 0;

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private static class BrowserTab {
        WebView web;
        boolean privateMode;

        BrowserTab(WebView web, boolean privateMode) {
            this.web = web;
            this.privateMode = privateMode;
        }
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        buildInterface();
        addTab(false, "https://www.google.com");
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private TextView button(String text) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextColor(Color.WHITE);
        v.setTextSize(19);
        v.setGravity(Gravity.CENTER);
        v.setBackground(rounded(PANEL, 18));
        v.setPadding(dp(10), dp(6), dp(10), dp(6));
        return v;
    }

    private void buildInterface() {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        // Outer spacing around the complete browser interface.
        root.setPadding(dp(12), dp(10), dp(12), dp(10));

        setContentView(root);

        // HEADER
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(4), dp(4), dp(4), dp(8));

        title = new TextView(this);
        title.setText("MY BROWSER");
        title.setTextColor(Color.WHITE);
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT_BOLD);

        header.addView(
                title,
                new LinearLayout.LayoutParams(0, -2, 1)
        );

        root.addView(header);

        // ADDRESS BAR
        LinearLayout addressRow = new LinearLayout(this);
        addressRow.setOrientation(LinearLayout.HORIZONTAL);
        addressRow.setGravity(Gravity.CENTER_VERTICAL);

        address = new EditText(this);
        address.setSingleLine(true);
        address.setTextSize(15);
        address.setTextColor(Color.WHITE);
        address.setHintTextColor(Color.rgb(145, 155, 174));
        address.setHint("Search or enter address");
        address.setPadding(dp(16), 0, dp(12), 0);
        address.setSelectAllOnFocus(true);
        address.setImeOptions(EditorInfo.IME_ACTION_GO);
        address.setBackground(rounded(PANEL, 28));

        address.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_URI
        );

        addressRow.addView(
                address,
                new LinearLayout.LayoutParams(0, dp(48), 1)
        );

        TextView go = button("➜");

        LinearLayout.LayoutParams goParams =
                new LinearLayout.LayoutParams(dp(46), dp(46));

        goParams.setMargins(dp(8), 0, 0, 0);
        addressRow.addView(go, goParams);

        go.setOnClickListener(v -> navigateFromAddress());

        address.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO ||
                    (event != null &&
                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                navigateFromAddress();
                return true;
            }
            return false;
        });

        LinearLayout.LayoutParams addressParams =
                new LinearLayout.LayoutParams(-1, -2);

        addressParams.setMargins(0, dp(4), 0, dp(8));
        root.addView(addressRow, addressParams);

        // WEBSITE VIEW
        webContainer = new FrameLayout(this);

        root.addView(
                webContainer,
                new LinearLayout.LayoutParams(-1, 0, 1)
        );

        // BOTTOM TOOLBAR
        toolbar = new LinearLayout(this);
        toolbar.setGravity(Gravity.CENTER);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setPadding(0, 0, 0, 0);

        LinearLayout.LayoutParams toolbarParams =
                new LinearLayout.LayoutParams(-1, dp(50));

        toolbarParams.setMargins(0, dp(10), 0, dp(2));
        root.addView(toolbar, toolbarParams);

        addTool("‹", () -> goBack());
        addTool("›", () -> goForward());
        addTool("⌂", () -> loadHome());

        // Chrome-style centred tab switcher.
        tabCount = button("▢ 1");
        tabCount.setTextSize(16);

        LinearLayout.LayoutParams tabParams =
                new LinearLayout.LayoutParams(0, dp(48), 1);

        tabParams.setMargins(dp(4), 0, dp(4), 0);
        toolbar.addView(tabCount, tabParams);

        tabCount.setOnClickListener(v -> showTabs());

        addTool("⋮", () -> showMenu());
    }

    private void addTool(String label, Runnable action) {
        TextView v = button(label);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(0, dp(48), 1);

        p.setMargins(dp(4), 0, dp(4), 0);

        toolbar.addView(v, p);
        v.setOnClickListener(view -> action.run());
    }

    private WebView createWebView(boolean privateMode) {

        WebView web = new WebView(this);
        WebSettings s = web.getSettings();

        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadsImagesAutomatically(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);

        if (privateMode) {
            s.setCacheMode(WebSettings.LOAD_NO_CACHE);
            web.clearCache(true);
            web.clearHistory();
        }

        web.setBackgroundColor(Color.WHITE);

        web.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(
                    WebView view,
                    String url,
                    Bitmap favicon) {

                if (view == currentWeb()) {
                    address.setText(url);
                    title.setText("LOADING");
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                if (view == currentWeb()) {
                    address.setText(url);
                    title.setText(
                            privateMode ? "PRIVATE TAB" : "MY BROWSER"
                    );
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request) {
                return false;
            }
        });

        web.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(
                    WebView view,
                    String pageTitle) {

                if (view == currentWeb() &&
                        !TextUtils.isEmpty(pageTitle)) {

                    title.setText(
                            pageTitle.length() > 22
                                    ? pageTitle.substring(0, 22) + "…"
                                    : pageTitle
                    );
                }
            }
        });

        return web;
    }

    private WebView currentWeb() {
        if (tabs.isEmpty() ||
                activeTab < 0 ||
                activeTab >= tabs.size()) {
            return null;
        }

        return tabs.get(activeTab).web;
    }

    private void updateTabCount() {
        if (tabCount != null) {
            tabCount.setText("▢ " + tabs.size());
        }
    }

    private void addTab(boolean privateMode, String url) {

        WebView web = createWebView(privateMode);

        tabs.add(new BrowserTab(web, privateMode));
        activeTab = tabs.size() - 1;

        webContainer.removeAllViews();
        webContainer.addView(
                web,
                new FrameLayout.LayoutParams(-1, -1)
        );

        updateTabCount();
        web.loadUrl(url);
    }

    private void switchTab(int index) {

        if (index < 0 || index >= tabs.size()) return;

        activeTab = index;

        webContainer.removeAllViews();
        webContainer.addView(
                currentWeb(),
                new FrameLayout.LayoutParams(-1, -1)
        );

        address.setText(currentWeb().getUrl());
        updateTabCount();

        title.setText(
                tabs.get(index).privateMode
                        ? "PRIVATE TAB"
                        : "MY BROWSER"
        );
    }

    private void closeTab(int index) {

        if (tabs.size() <= 1) {
            tabs.get(0).web.loadUrl("https://www.google.com");
            return;
        }

        BrowserTab tab = tabs.remove(index);

        tab.web.stopLoading();
        tab.web.loadUrl("about:blank");
        tab.web.destroy();

        if (activeTab >= tabs.size()) {
            activeTab = tabs.size() - 1;
        } else if (index < activeTab) {
            activeTab--;
        }

        switchTab(activeTab);
    }

    private void showTabs() {

        String[] labels = new String[tabs.size()];

        for (int i = 0; i < tabs.size(); i++) {

            String url = tabs.get(i).web.getUrl();

            labels[i] = (i == activeTab ? "● " : "") +
                    (tabs.get(i).privateMode ? "[Private] " : "") +
                    (url == null ? "New tab" : url);
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("Open tabs")
                .setItems(labels, (dialog, which) -> switchTab(which))
                .setNegativeButton(
                        "Close active tab",
                        (dialog, which) -> closeTab(activeTab)
                )
                .setPositiveButton("Done", null)
                .show();
    }

    private void navigateFromAddress() {

        String input = address.getText().toString().trim();

        if (input.isEmpty()) return;

        String url;

        if (input.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) {
            url = input;
        } else if (input.contains(".") && !input.contains(" ")) {
            url = "https://" + input;
        } else {
            url = "https://www.google.com/search?q=" +
                    Uri.encode(input);
        }

        ((InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE
        )).hideSoftInputFromWindow(address.getWindowToken(), 0);

        currentWeb().loadUrl(url);
        currentWeb().requestFocus();
    }

    private void loadHome() {
        address.setText("https://www.google.com");
        currentWeb().loadUrl("https://www.google.com");
    }

    private void goBack() {
        if (currentWeb() != null && currentWeb().canGoBack()) {
            currentWeb().goBack();
        }
    }

    private void goForward() {
        if (currentWeb() != null && currentWeb().canGoForward()) {
            currentWeb().goForward();
        }
    }

    private void showMenu() {

        PopupMenu menu = new PopupMenu(
                this,
                toolbar.getChildAt(4)
        );

        menu.getMenu().add("New tab");
        menu.getMenu().add("New private tab");
        menu.getMenu().add("Reload");
        menu.getMenu().add("Share page");
        menu.getMenu().add("Desktop site");
        menu.getMenu().add("Clear browsing data");

        menu.setOnMenuItemClickListener(item -> {

            String choice = item.getTitle().toString();

            if (choice.equals("New tab")) {
                addTab(false, "https://www.google.com");

            } else if (choice.equals("New private tab")) {
                addTab(true, "https://www.google.com");

            } else if (choice.equals("Reload")) {
                currentWeb().reload();

            } else if (choice.equals("Share page")) {
                sharePage();

            } else if (choice.equals("Desktop site")) {
                toggleDesktop();

            } else if (choice.equals("Clear browsing data")) {
                clearData();
            }

            return true;
        });

        menu.show();
    }

    private void sharePage() {

        String url = currentWeb().getUrl();

        if (url == null) return;

        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, url);

        startActivity(
                Intent.createChooser(send, "Share page")
        );
    }

    private void toggleDesktop() {

        WebSettings s = currentWeb().getSettings();

        boolean desktop =
                s.getUserAgentString().contains("Mobile");

        if (desktop) {
            s.setUserAgentString(null);
            s.setUseWideViewPort(true);
            s.setLoadWithOverviewMode(true);

        } else {
            String ua = s.getUserAgentString()
                    .replace(" Mobile", "")
                    .replace(" Android", "");

            s.setUserAgentString(ua);
        }

        currentWeb().reload();
    }

    private void clearData() {

        for (BrowserTab tab : tabs) {
            tab.web.clearHistory();
            tab.web.clearCache(true);
            tab.web.clearFormData();
        }

        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();

        Toast.makeText(
                this,
                "Local browser data cleared",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onBackPressed() {

        if (currentWeb() != null && currentWeb().canGoBack()) {
            currentWeb().goBack();

        } else if (tabs.size() > 1) {
            closeTab(activeTab);

        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {

        for (BrowserTab tab : tabs) {
            tab.web.destroy();
        }

        tabs.clear();
        super.onDestroy();
    }
}
