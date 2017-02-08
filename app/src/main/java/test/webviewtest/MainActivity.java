package test.webviewtest;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    WebView webview;
    String url;

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

            public void onPageFinished(WebView view, String url) {
                webview.loadUrl(" javascript:(function() { var video = document.getElementsByTagName('video')[0]; video.loop = false; video.addEventListener('ended', function() { video.currentTime=0.1; video.play(); }, false);  video.play(); })()");
            }
        });
        webview.loadUrl(url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webview = (WebView) findViewById(R.id.webview);
        webview.setWebChromeClient(new WebChromeClient());
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        url = "https://ui.jukko.com/dist/weforest/intro.html";
        showUrl(url);
    }
}
