package com.example.csci571_hw9;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class allTimePrice extends Fragment {

    ArrayList<double[]> alltimeData;
    ArrayList<Long> time;
    ArrayList<Long> volume;
    String ticker;
    String alltimeInfo;

    public allTimePrice(ArrayList<double[]> alltimeData, ArrayList<Long> time, ArrayList<Long> volume, String ticker) {
        this.alltimeData = alltimeData;
        this.time = time;
        this.volume = volume;
        this.ticker = ticker;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_all_time_price, container, false);
        long[] timeArray = new long[time.size()];
        long[] volumeArray = new long[time.size()];
        double[][]  alltimeArray = new double[alltimeData.size()][4];
        Log.d("alltimeData", "" + alltimeData.get(0)[0]);
        for (int i =0; i < time.size(); i++) {
            timeArray[i] = time.get(i);
            volumeArray[i] = volume.get(i);
            alltimeArray[i][0] = alltimeData.get(i)[0];
            alltimeArray[i][1] = alltimeData.get(i)[1];
            alltimeArray[i][2] = alltimeData.get(i)[2];
            alltimeArray[i][3] = alltimeData.get(i)[3];
        }
        alltimeInfo = "[";
        for (int i = 0; i < alltimeArray.length - 1; i++) {
            alltimeInfo += Arrays.toString(alltimeArray[i]);
            alltimeInfo += ",";
        }
        alltimeInfo += Arrays.toString(alltimeArray[alltimeArray.length - 1]);
        alltimeInfo += "]";

        WebView webView= (WebView)v.findViewById(R.id.webviewAll);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webView.loadUrl("javascript:loadALllData(\""+ Arrays.toString(timeArray) + "\",\""+ Arrays.toString(volumeArray) + "\",\"" + alltimeInfo + "\",\""+ ticker + "\")");
            }
        });
        webView.loadUrl("file:///android_asset/allPrice.html");
//        TextView test = (TextView)v.findViewById(R.id.testText);
//        test.setText(alltimeInfo);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}