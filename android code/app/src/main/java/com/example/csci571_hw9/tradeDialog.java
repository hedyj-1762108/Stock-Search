package com.example.csci571_hw9;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class tradeDialog extends AppCompatDialogFragment {
    private TextView tradeTitle;
    private TextView numOfPrice;
    private TextView tradeTicker;
    private TextView total;
    private TextView numOfShare;
    String value;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(), R.style.MyRounded_MaterialComponents_MaterialAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.tradedialog,null);
        builder.setView(view);


        tradeTitle = view.findViewById(R.id.tradeTitle);
        tradeTitle.setText(getArguments().getString("companyName") + " shares");
        numOfPrice = view.findViewById(R.id.numOfPrice);
        numOfPrice.setText(""+ getArguments().getDouble("price"));
        tradeTicker = view.findViewById(R.id.tradeTicker);
        tradeTicker.setText(""+ getArguments().getString("ticker"));
        total = view.findViewById(R.id.finalPrice);
        EditText editText = (EditText) view.findViewById(R.id.input);
        numOfShare = view.findViewById(R.id.numOfShare);
        editText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                value = editText.getText().toString();
                if (isInt(value)) {
                    int input_share = Integer.parseInt(value);
                    numOfShare.setText("" + input_share);
                    double totalValue = Math.round(input_share * getArguments().getDouble("price")*100.0)/100.0;
                    total.setText("" + totalValue);

                } else {
                    total.setText("0.00");
                    numOfShare.setText("0");
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        return builder.create();

    }

    public static tradeDialog newInstance(String companyName, String ticker, double price) {
        tradeDialog fragment = new tradeDialog();
        Bundle bundle = new Bundle();
        bundle.putString("companyName", companyName);
        bundle.putString("ticker", ticker);
        bundle.putDouble("price", price);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static boolean isInt(String str) {
        try {
            @SuppressWarnings("unused")
            int x = Integer.parseInt(str);
            return true; //String is an Integer
        } catch (NumberFormatException e) {
            return false; //String is not an Integer
        }
    }

}
