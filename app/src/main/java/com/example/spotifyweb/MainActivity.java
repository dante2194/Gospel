package com.example.spotifyweb;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String SPOTIFY_URL = "https://open.spotify.com/";
    private static final String USER_AGENT =
        "Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/140.0.0.0 Mobile Safari/537.36";
    private static final int STORAGE_PERMISSION_REQUEST = 9101;

    private WebView webView;
    private String pendingDownloadUrl;
    private String pendingDownloadUserAgent;
    private String pendingDownloadContentDisposition;
    private String pendingDownloadMimeType;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        // Keep all normal navigation in this WebView. Popups are also routed
        // back into the same WebView instead of launching another browser.
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setUserAgentString(USER_AGENT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new InAppWebViewClient());
        webView.setWebChromeClient(new InAppPopupClient());
        webView.setDownloadListener(new InAppDownloadListener());

        if (savedInstanceState == null) webView.loadUrl(SPOTIFY_URL);
        else webView.restoreState(savedInstanceState);

        startPlaybackService();
        requestNotificationPermission();
    }

    private void startPlaybackService() {
        android.content.Intent serviceIntent =
            new android.content.Intent(this, PlaybackKeepAliveService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(serviceIntent);
        else startService(serviceIntent);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 9001);
        }
    }

    private void requestLegacyStorageThenDownload() {
        if (Build.VERSION.SDK_INT <= 28 &&
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_REQUEST
            );
            return;
        }
        enqueueDownload(
            pendingDownloadUrl,
            pendingDownloadUserAgent,
            pendingDownloadContentDisposition,
            pendingDownloadMimeType
        );
        clearPendingDownload();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enqueueDownload(
                    pendingDownloadUrl,
                    pendingDownloadUserAgent,
                    pendingDownloadContentDisposition,
                    pendingDownloadMimeType
                );
            } else {
                Toast.makeText(this, "Storage permission is required for this download", Toast.LENGTH_LONG).show();
            }
            clearPendingDownload();
        }
    }

    private void clearPendingDownload() {
        pendingDownloadUrl = null;
        pendingDownloadUserAgent = null;
        pendingDownloadContentDisposition = null;
        pendingDownloadMimeType = null;
    }

    private String cleanFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) fileName = "download";

        // Remove the requested marker from generated/downloaded file names.
        fileName = fileName.replaceAll("(?i)SpotiDown\\.App", "");
        fileName = fileName.replaceAll("[\\r\\n\\t]", " ");
        fileName = fileName.replaceAll("\\s{2,}", " ").trim();
        fileName = fileName.replaceAll("^[. _-]+", "");
        fileName = fileName.replaceAll("[. _-]+$", "");

        if (fileName.isEmpty()) fileName = "download";
        return fileName;
    }

    private void enqueueDownload(String url, String userAgent, String contentDisposition, String mimeType) {
        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(this, "Download URL is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(true);

            String cookie = CookieManager.getInstance().getCookie(url);
            if (cookie != null && !cookie.isEmpty()) request.addRequestHeader("Cookie", cookie);
            if (userAgent != null && !userAgent.isEmpty()) request.addRequestHeader("User-Agent", userAgent);

            String guessed = android.webkit.URLUtil.guessFileName(url, contentDisposition, mimeType);
            guessed = cleanFileName(guessed);

            if (mimeType != null && !mimeType.isEmpty() && !guessed.contains(".")) {
                String extension = android.webkit.MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(mimeType.toLowerCase(Locale.US));
                if (extension != null && !extension.isEmpty()) guessed += "." + extension;
            }

            request.setTitle(guessed);
            request.setDescription("Saved by Spotify Web");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, guessed);

            DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (manager != null) {
                manager.enqueue(request);
                Toast.makeText(this, "Download started: " + guessed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Download service unavailable", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String loadAsset(String name) {
        try (InputStream in = getAssets().open(name);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
            return out.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            return "";
        }
    }

    private void injectAsset(WebView view, String assetName) {
        String script = loadAsset(assetName);
        if (script.isEmpty()) return;
        String escaped = script
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "");
        view.evaluateJavascript(
            "javascript:(function(){try{eval('" + escaped + "');}" +
            "catch(e){console.error(e);}})();", null);
    }

    private void injectPageScripts(WebView view) {
        injectAsset(view, "userscripts/spotify_declutter.user.js");
        injectAsset(view, "userscripts/app_cleanup.user.js");
    }

    private class InAppWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            injectPageScripts(view);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            // Never hand navigation to another browser/app.
            return false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Never hand navigation to another browser/app.
            return false;
        }
    }

    private class InAppPopupClient extends WebChromeClient {
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {
            final WebView popup = new WebView(MainActivity.this);
            WebSettings s = popup.getSettings();
            s.setJavaScriptEnabled(true);
            s.setDomStorageEnabled(true);
            s.setJavaScriptCanOpenWindowsAutomatically(true);
            s.setSupportMultipleWindows(true);
            s.setUserAgentString(USER_AGENT);

            popup.setDownloadListener(new InAppDownloadListener());
            popup.setWebChromeClient(this);
            popup.setWebViewClient(new WebViewClient() {
                private boolean redirected;

                private boolean redirectIntoMain(String url) {
                    if (!redirected && url != null && !url.equals("about:blank")) {
                        redirected = true;
                        webView.loadUrl(url);
                        popup.stopLoading();
                        popup.destroy();
                    }
                    return true;
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest request) {
                    return redirectIntoMain(request.getUrl().toString());
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView v, String url) {
                    return redirectIntoMain(url);
                }

                @Override
                public void onPageFinished(WebView v, String url) {
                    if (url != null && !url.equals("about:blank")) redirectIntoMain(url);
                }
            });

            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(popup);
            resultMsg.sendToTarget();
            return true;
        }
    }

    private class InAppDownloadListener implements DownloadListener {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                    String mimetype, long contentLength) {
            pendingDownloadUrl = url;
            pendingDownloadUserAgent = userAgent;
            pendingDownloadContentDisposition = contentDisposition;
            pendingDownloadMimeType = mimetype;
            requestLegacyStorageThenDownload();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.setDownloadListener(null);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
