package test.webviewtest;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity {

    WebView webview;
    String url;
    Cache cache;
    OkHttpClient okHttpClient;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.change) {
            showChangeDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void showChangeDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog, null);
        final EditText editText = (EditText) view.findViewById(R.id.editText);
        editText.setText(url);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                url = editText.getText().toString();
                showUrl(url);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showUrl(String url) {
        webview.setWebViewClient(new WebViewClient() {

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url_) {
                if (urlShouldBeHandledByWebView(url_) || !isNetworkAvailable()) {
                    return super.shouldInterceptRequest(view, url_);
                }
                Log.d("WebView", "handled by webview " + url_);

                return handleRequestViaOkHttp(url_);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url_ = request.getUrl().toString();
                if (urlShouldBeHandledByWebView(url_) || !isNetworkAvailable()) {
                    return super.shouldInterceptRequest(view, request);
                }
                Log.d("WebView", "handled by webview " + url_);
                return handleRequestViaOkHttp(url_);
            }
        });
        webview.loadUrl(url);
        Log.d("WebView", "cache.hitCount() = " + cache.hitCount());
        Log.d("WebView", "cache.networkCount() = " + cache.networkCount());
        Log.d("WebView", "cache.requestCount() = " + cache.requestCount());

    }

    private boolean urlShouldBeHandledByWebView(String url) {
        return url.startsWith("file:///");
    }

    private WebResourceResponse handleRequestViaOkHttp(String url) {
        final Call call = okHttpClient.newCall(new Request.Builder().url(url)/*.cacheControl(CacheControl.FORCE_CACHE)*/.build());
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e1) {
            e1.printStackTrace();
            Log.e("WebView Test", e1.getMessage());
            //TODO handle IO excepiton
        }


        Log.d("Headers Webview", url);
        String contType = response.header("Content-Type", "text/plain");
        if (contType.contains(";")) {
            contType = contType.split(";")[0];
        }

        WebResourceResponse wrr = new WebResourceResponse(
                contType,
                response.header("Content-Encoding", "utf-8"),  // Again, you can set another encoding as default
                response.body().byteStream()
        );
        return wrr;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webview = (WebView) findViewById(R.id.webview);
        webview.setWebChromeClient(new WebChromeClient());
        WebSettings settings = webview.getSettings();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.setMediaPlaybackRequiresUserGesture(false);
        }

        int cacheSize = 20 * 1024 * 1024;
        cache = new Cache(getCacheDir().getAbsoluteFile(), cacheSize);
        HttpLoggingInterceptor loggr = new HttpLoggingInterceptor();
        loggr.setLevel(HttpLoggingInterceptor.Level.BODY);
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                response.newBuilder().header("Cache-Control", "max-age=60000").build();
                return response;
                //Request request = chain.request();
                //request = request.newBuilder().addHeader("Cache-Control", "max-age=60000").build();
                //return chain.proceed(request);
            }
        };

        okHttpClient = new OkHttpClient.Builder().cache(cache).addNetworkInterceptor(loggr).addInterceptor(interceptor).addNetworkInterceptor(interceptor).build();
        url = "https://avatars3.githubusercontent.com/u/398556?v=3&s=460";


        Call call = okHttpClient.newCall(new Request.Builder().url(url).build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("Headers Webview", "Failure");
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Failure", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("Headers Webview", "Response");
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Response", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }
        });


        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setJavaScriptEnabled(true);


        webview.postDelayed(new Runnable() {
            @Override
            public void run() {
                showUrl(url);
            }
        }, 9000);

    }


    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

