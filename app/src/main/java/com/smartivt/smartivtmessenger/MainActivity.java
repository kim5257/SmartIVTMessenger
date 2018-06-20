package com.smartivt.smartivtmessenger;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity";
    final String WEB_TAG = "WebView";

    final String webUserAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:60.0) Gecko/20100101 Firefox/60.0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = (WebView)findViewById(R.id.webview_main);

        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString(webUserAgent);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            webSettings.setTextZoom(100);
        }

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(WEB_TAG, consoleMessage.message() + " (" + consoleMessage.messageLevel() + ")\t" + consoleMessage.sourceId());
                return super.onConsoleMessage(consoleMessage);
            }
        });

        webView.addJavascriptInterface(new JSPInterface(), "android");

        webView.loadUrl("http://testapp.smartivt.com:3000");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean goBackFlag = false;

        if ( event.getAction() == KeyEvent.ACTION_DOWN ) {
            switch(keyCode)
            {
            case KeyEvent.KEYCODE_BACK:
                {
                    WebView webView = (WebView)findViewById(R.id.webview_main);
                    if (webView.canGoBack()) {
                        webView.goBack();
                        goBackFlag = true;
                    }
                }
                break;
            }
        }

        if ( goBackFlag ) {
            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
