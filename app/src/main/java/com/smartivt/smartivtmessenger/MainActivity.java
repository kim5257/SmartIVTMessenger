package com.smartivt.smartivtmessenger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity";
    final String WEB_TAG = "WebView";
    final String webUserAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:60.0) Gecko/20100101 Firefox/60.0";

    private BackPressCloser backPressCloser;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WebView webView = (WebView)findViewById(R.id.webview_main);

        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString(webUserAgent);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            webSettings.setTextZoom(100);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                webView.loadUrl("about:blank");

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setPositiveButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.finish();
                    }
                });

                builder.setCancelable(false);
                builder.setMessage(getString(R.string.connect_error));
                builder.show();
                //super.onReceivedError(view, request, error);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(WEB_TAG, consoleMessage.message() + " (" + consoleMessage.messageLevel() + ")\t" + consoleMessage.sourceId());
                return super.onConsoleMessage(consoleMessage);
            }
        });

        webView.addJavascriptInterface(new JSPInterface(), "android");

        webView.loadUrl(getString(R.string.server_url));

        backPressCloser = new BackPressCloser(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean backKeyFlag = false;
        boolean closeFlag = false;

        if ( event.getAction() == KeyEvent.ACTION_DOWN ) {
            switch(keyCode)
            {
            case KeyEvent.KEYCODE_BACK:
                {
                    WebView webView = (WebView)findViewById(R.id.webview_main);

                    Log.d(WEB_TAG, "URL: " + webView.getUrl());
                    Log.d(WEB_TAG, "OriginalURL: " + webView.getOriginalUrl());

                    if ( isTopUrl(webView.getUrl()) ) {
                        // 상위 위치이므로 닫기 시도
                        closeFlag = true;
                    }
                    else if (webView.canGoBack()) {
                        webView.goBack();
                    }
                    else {
                        closeFlag = true;
                    }

                    backKeyFlag = true;
                }
                break;
            }

            if ( closeFlag ) {
                backPressCloser.onBackPress();
            }
        }

        if ( !backKeyFlag ) {
            return super.onKeyDown(keyCode, event);
        }
        else {
            return true;
        }
    }

    private boolean isTopUrl (String url) {
        boolean ret = false;
        Uri uri = Uri.parse(url);
        Log.d(WEB_TAG, "Path: " + uri.getEncodedPath());

        String topUris[] = getResources().getStringArray(R.array.top_uris);

        for(String item: topUris) {
            if ( item.equals(uri.getEncodedPath()) ) {
                ret = true;
                break;
            }
        }

        return ret;
    }
}
