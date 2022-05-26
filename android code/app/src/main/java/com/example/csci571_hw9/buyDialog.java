package com.example.csci571_hw9;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class buyDialog extends AppCompatDialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(), R.style.MyRounded_MaterialComponents_MaterialAlertDialog_green);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.buydialog,null);
        builder.setView(view);
        return builder.create();
    }

//    public static buyDialog newInstance(String numOfShare, String ticker) {
//        buyDialog fragment = new buyDialog();
//        Bundle bundle = new Bundle();
////        bundle.putString("companyName", companyName);
////        bundle.putString("ticker", ticker);
////        bundle.putDouble("price", price);
//        fragment.setArguments(bundle);
//        return fragment;
//    }
}
