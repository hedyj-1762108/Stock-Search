package com.example.csci571_hw9;

import androidx.annotation.NonNull;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.*;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class portfolioAdaptor extends RecyclerView.Adapter<portfolioAdaptor.ViewHolder>{

    private JSONArray tickersOnPortfolio;


    public portfolioAdaptor (JSONArray tickersOnPortfolio) {
        this.tickersOnPortfolio = tickersOnPortfolio;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.portfolio, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String tickerName = "";
        int numOfShare = 0;
        Double price = 0.0;
        Double change_in_price = 0.0;
        Double change_in_percent = 0.0;
        String price_change_text = "";
        Resources res = holder.itemView.getContext().getResources();
        try {
            tickerName = tickersOnPortfolio.getJSONObject(position).getString("ticker");
            numOfShare = tickersOnPortfolio.getJSONObject(position).getInt("share");
            price = tickersOnPortfolio.getJSONObject(position).getDouble("market_value");
            change_in_price = tickersOnPortfolio.getJSONObject(position).getDouble("change_in_price");
            change_in_percent = tickersOnPortfolio.getJSONObject(position).getDouble("change_in_percent");
            price_change_text = "$ " + change_in_price +" ( "+ change_in_percent +"% )";

        } catch (JSONException e) {
            e.printStackTrace();
        }
        holder.ticker.setText(tickerName);
        holder.share.setText(numOfShare + " shares");
        holder.price.setText("$" + price);
        holder.priceChange.setText(price_change_text);
        if (change_in_price > 0) {
            holder.imageView.setVisibility(View.VISIBLE);
            holder.imageViewdown.setVisibility(View.GONE);
            holder.priceChange.setTextColor(res.getColor(R.color.green));
        } else if (change_in_price < 0) {
            holder.imageViewdown.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
            holder.priceChange.setTextColor(res.getColor(R.color.red));
        } else {
            holder.imageView.setVisibility(View.GONE);
            holder.imageViewdown.setVisibility(View.GONE);
            holder.priceChange.setTextColor(res.getColor(R.color.black));
        }
        String finalTickerName = tickerName;
        if (getItemCount() >= 0) {
            holder.arrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //goes to new activity passing the item name
                    Intent intent = new Intent(v.getContext(), Detail.class);
                    intent.putExtra("ticker", finalTickerName);
                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return tickersOnPortfolio.length();
    }

    public void updateData(JSONArray data) throws JSONException {
        tickersOnPortfolio = data;
    }

    public void notifyItemMoved() {

    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView ticker, price, share, priceChange;
        ImageView imageView, imageViewdown, arrow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ticker = itemView.findViewById(R.id.ticker);
            price = itemView.findViewById(R.id.price);
            share =  itemView.findViewById(R.id.share);
            priceChange = itemView.findViewById(R.id.priceChange);
            imageView = itemView.findViewById(R.id.imageView);
            imageViewdown = itemView.findViewById(R.id.imageViewdown);
            arrow = itemView.findViewById(R.id.arrow);
        }
    }
}


