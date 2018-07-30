package com.smartivt.smartivtmessenger;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity";
    final String WEB_TAG = "WebView";
    final String webUserAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:60.0) Gecko/20100101 Firefox/60.0";

    private static final String TYPE_IMAGE = "image/*";
    private static final int INPUT_FILE_REQUEST_CODE = 1;

    private BackPressCloser backPressCloser;

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if ( requestCode == 0 ) {
            if ( grantResults[0] != 0 || grantResults[1] != 0 ) {
                Toast.makeText(this, getString(R.string.permission_error), Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( requestCode == INPUT_FILE_REQUEST_CODE && resultCode == RESULT_OK ) {
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                if ( mFilePathCallback == null ) {
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }

                Uri[] results = new Uri[]{getResultUri(data)};

                mFilePathCallback.onReceiveValue(results);
                mFilePathCallback = null;
            }
            else {
                if ( mUploadMessage == null ) {
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }

                Uri result = getResultUri(data);

                Log.d(getClass().getName(), "openFileChooser: " + result);
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
        else {
            if ( mFilePathCallback != null ) {
                mFilePathCallback.onReceiveValue(null);
            }
            if ( mUploadMessage != null ) {
                mUploadMessage.onReceiveValue(null);
            }

            mFilePathCallback = null;
            mUploadMessage = null;

            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private Uri getResultUri (Intent data) {
        Uri result = null;
        if ( data == null || TextUtils.isEmpty(data.getDataString()) ) {
            if ( mCameraPhotoPath != null ) {
                result = Uri.parse(mCameraPhotoPath);
            }
        }
        else {
            String filePath = "";
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                filePath = data.getDataString();
            }
            else {
                filePath = "file:" + RealPathUtil.getRealPath(this, data.getData());
            }

            result = Uri.parse(filePath);
        }

        return result;
    }

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

                if ( isFinishing() == false ) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setPositiveButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            MainActivity.this.finish();
                        }
                    });

                    try {
                        builder.setCancelable(false);
                        builder.setMessage(getString(R.string.connect_error));
                        builder.show();
                        //super.onReceivedError(view, request, error);
                    }
                    catch(Exception err) {
                        // Do nothing.
                    }
                }
            }
        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart ( String url,
                                            String userAgent,
                                            String contentDisposition,
                                            String mimeType,
                                            long contentLength) {
                String downloadCaption = getString(R.string.download_image);
                String downloadingCaption = getString(R.string.downloading_image);

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                request.setDescription(downloadCaption);
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));

                DownloadManager downloadMgr = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                downloadMgr.enqueue(request);
                Toast.makeText(getApplicationContext(), downloadingCaption, Toast.LENGTH_LONG).show();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(WEB_TAG, consoleMessage.message() + " (" + consoleMessage.messageLevel() + ")\t" + consoleMessage.sourceId());
                return super.onConsoleMessage(consoleMessage);
            }

            // For Android Version < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                //System.out.println("WebViewActivity OS Version : " + Build.VERSION.SDK_INT + "\t openFC(VCU), n=1");
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(TYPE_IMAGE);
                startActivityForResult(intent, INPUT_FILE_REQUEST_CODE);
            }

            // For 3.0 <= Android Version < 4.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                //System.out.println("WebViewActivity 3<A<4.1, OS Version : " + Build.VERSION.SDK_INT + "\t openFC(VCU,aT), n=2");
                openFileChooser(uploadMsg, acceptType, "");
            }

            // For 4.1 <= Android Version < 5.0
            public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
                Log.d(getClass().getName(), "openFileChooser : "+acceptType+"/"+capture);
                mUploadMessage = uploadFile;
                imageChooser();
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                Log.d(WEB_TAG, "onShowFileChooser");

                if ( mFilePathCallback != null ) {
                    mFilePathCallback.onReceiveValue(null);
                }

                mFilePathCallback = filePathCallback;
                imageChooser();
                return true;
            }

            private void imageChooser() {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if ( takePictureIntent.resolveActivity(getPackageManager())!=null ) {
                    File photoFile = null;

                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    }
                    catch (IOException err) {
                        Log.e(getClass().getName(), "Unable to create Image File", err);
                    }

                    if ( photoFile != null ) {
                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    }
                    else {
                        takePictureIntent = null;
                    }
                }

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType(TYPE_IMAGE);

                Intent[] intentArray;
                if ( takePictureIntent != null ) {
                    intentArray = new Intent[]{takePictureIntent};
                }
                else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.select_image));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
            }

            private File createImageFile() throws IOException {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "IMG_" + timeStamp;

                File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

                return imageFile;
            }
        });

        webView.addJavascriptInterface(new JSPInterface(), "android");

        webView.loadUrl(getString(R.string.server_url));

        backPressCloser = new BackPressCloser(this);

        // 권한 확인
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) ||
                    (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        }
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
