package com.example.csci571_hw9;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Locale;

public class hourlyPrice extends Fragment {

    String data_point;
    String ticker;
    String color;

    public hourlyPrice(String data, String ticker, String color) {
        this.data_point = data;
        this.ticker = ticker;
        this.color = color;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        View root = inflater.inflate(R.layout.fragment_hourly_price, container, false);
        View v = inflater.inflate(R.layout.fragment_hourly_price, container, false);
        WebView webView= (WebView)v.findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
//                String data = "[" + data_point + "," + ticker + "," + color + "]";
                webView.loadUrl("javascript:loadHourlyData(\""+ data_point + "\",\""+ ticker.toUpperCase() + "\",\""+ color + "\")");
            }
        });
        webView.loadUrl("file:///android_asset/hourPrice.html");
//        TextView test = (TextView)v.findViewById(R.id.test);
//        test.setText(color);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}