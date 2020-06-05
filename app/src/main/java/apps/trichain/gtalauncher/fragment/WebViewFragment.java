package apps.trichain.gtalauncher.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import apps.trichain.gtalauncher.R;
import apps.trichain.gtalauncher.databinding.WebviewFragmentBinding;
import apps.trichain.gtalauncher.model.Links;
import apps.trichain.gtalauncher.util.SharedPrefsManager;
import apps.trichain.gtalauncher.util.util;
import apps.trichain.gtalauncher.viewModel.GameViewModel;

public class WebViewFragment extends DialogFragment {

    private WebviewFragmentBinding b;
    private SharedPrefsManager sharedPrefsManager;
    private GameViewModel viewModel;
    private static final String TAG = "WebViewFragment";

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = DataBindingUtil.inflate(inflater, R.layout.webview_fragment, container, false);
        sharedPrefsManager = SharedPrefsManager.getInstance(getContext());
        Links links = Links.create(sharedPrefsManager.getLinks());

        ViewModelProvider.AndroidViewModelFactory factory = new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication());
        viewModel = new ViewModelProvider(getActivity(), factory).get(GameViewModel.class);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(value -> {
        });
        cookieManager.setAcceptCookie(true);

        b.webview.getSettings().setJavaScriptEnabled(true);

        if (links != null) {
            Snackbar s = Snackbar.make(b.webview, "Click on the Download button to start download", BaseTransientBottomBar.LENGTH_INDEFINITE);
            s.show();

            WebChromeClient webChromeClient = new WebChromeClient() {
                @Override
                public void onReceivedTitle(WebView view, String title) {
                    getDialog().getWindow().setTitle(title); //Set Activity tile to page title.
                }
            };

            WebViewClient webViewClient = new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    view.loadUrl(String.valueOf(request.getUrl()));
                    //view.loadUrl("https://www.google.com");
                    return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    if (url.startsWith("https://dl4.apksum.com")) {
                        util.hideView(b.pbGamesLoading);
                        Log.e(TAG, "onPageFinished: OBB url " + url);
                        viewModel.updateDownloadURL(url);
                        s.dismiss();
                        dismiss();
                        //viewModel.updateDownloadURL("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-zip-file.zip");
                    } else {
                        //Toast.makeText(getContext(), "Download URL not found", Toast.LENGTH_SHORT).show();
                    }

                }
            };
            b.webview.setWebViewClient(webViewClient);
            b.webview.setWebChromeClient(webChromeClient);
            b.webview.loadUrl(links.getObbURL());
            //b.webview.loadUrl("https://www.google.com");

        }
        return b.getRoot();
    }
}