package com.leakdoctor.pro;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.webkit.WebResourceRequest;
import android.net.Uri;
import android.content.Intent;

public class MainActivity extends Activity {
    private WebView web;
    private PermissionRequest pending;
    private static final String HOME = "https://rlgnsekf-ux.github.io/leakdoctor-pro/";
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if ("https".equals(u.getScheme()) && "rlgnsekf-ux.github.io".equals(u.getHost()) && u.getPath().startsWith("/leakdoctor-pro/")) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) { }
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(PermissionRequest req) {
                runOnUiThread(() -> {
                    if (!HOME.startsWith(req.getOrigin().toString()) && !req.getOrigin().toString().equals("https://rlgnsekf-ux.github.io")) { req.deny(); return; }
                    boolean camera = false;
                    for (String resource : req.getResources()) if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource)) camera = true;
                    if (!camera) { req.deny(); return; }
                    pending = req;
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) approveCamera();
                    else requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
                });
            }
        });
        web.loadUrl(HOME);
    }
    private void approveCamera() {
        if (pending != null) { pending.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE}); pending = null; }
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == 101 && pending != null) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) approveCamera();
            else { pending.deny(); pending = null; }
        }
    }
    @Override public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
    @Override protected void onDestroy() { web.destroy(); super.onDestroy(); }
}
