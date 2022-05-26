package com.example.csci571_hw9;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class newsDialog extends AppCompatDialogFragment {
    private TextView NewsTitle;
    private TextView datetime;
    private TextView headline;
    private TextView summary;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(), R.style.MyRounded_MaterialComponents_MaterialAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.newsdialog,null);
        builder.setView(view);
        NewsTitle = view.findViewById(R.id.NewsTitle);
        datetime = view.findViewById(R.id.datetime);
        headline = view.findViewById(R.id.NewsHeadline);
        summary = view.findViewById(R.id.NewsSummary);
        String title= getArguments().getString("title");
        String time= getArguments().getString("time");
        NewsTitle.setText(title);
        datetime.setText(time);
        headline.setText(getArguments().getString("headline"));
        summary.setText(getArguments().getString("summary"));

        ImageButton chrome = (ImageButton) view.findViewById(R.id.chrome);
        chrome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(getArguments().getString("url")));
                startActivity(intent);
            }
        });

        ImageButton twitter = (ImageButton) view.findViewById(R.id.twitter);
        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                String twitter_url = "https://twitter.com/intent/tweet?text=" + getArguments().getString("headline") + "&url="
                        + getArguments().getString("url");
                intent.setData(Uri.parse(twitter_url));
                startActivity(intent);
            }
        });

        ImageButton facebook = (ImageButton) view.findViewById(R.id.facebook);
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                String facebook_url = "https://www.facebook.com/sharer/sharer.php?u=" + getArguments().getString("url") + "&src=sdkpreparse";
                intent.setData(Uri.parse(facebook_url));
                startActivity(intent);
            }
        });

        return builder.create();
    }

    public static newsDialog newInstance(String your_message, String time, String headline, String summary, String url) {
        newsDialog fragment = new newsDialog();
        Bundle bundle = new Bundle();
        bundle.putString("title", your_message);
        bundle.putString("time", time);
        bundle.putString("headline", headline);
        bundle.putString("summary", summary);
        bundle.putString("url", url);
        fragment.setArguments(bundle);
        return fragment;
    }


}
