package com.example.csci571_hw9;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
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

import java.util.*;
import java.text.SimpleDateFormat;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView listView;
    private ArrayAdapter<String> arrayAdapter;
    //date time variables
    private TextView dateTimeDisplay;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String date;
    // recycler views
    private RecyclerView recyclerPortfolioView;
    private RecyclerView recyclerFavView;
    private portfolioAdaptor recyclerPortfolioAdaptor;
    private favoriteAdaptor recyclerFavoriteAdaptor;
    private List<String> portfolioTickers;
    // footer
    private TextView footer;

    // for control onResume
    private boolean shouldExecuteOnResume;

    // sharedPreferences
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private JSONArray boughtShares = new JSONArray();
    private float moneyInPocket = 25000;
    private JSONArray favList = new JSONArray();

    // Api CALL
    //private final info to portfolio Adaptor
    private JSONArray final_portfolio_data = new JSONArray();
    // fav data
    private JSONArray final_fav_data = new JSONArray();


    //autoComplete
    private SearchView.SearchAutoComplete searchAutoComplete;
    private ArrayAdapter<String> newsAdapter;

    //procress bar
    private ProgressBar processBar;
    private TextView nwValue;

    // net worth
    private double netWorth;

    // update data
    private Handler handler = new Handler();
    private Runnable runnable;
    private int delay = 15*1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shouldExecuteOnResume = false;
        setTheme(R.style.Theme_CSCI571hw9);
        setContentView(R.layout.activity_main);
        processBar = findViewById(R.id.processBar);
        processBar.setVisibility(View.VISIBLE);


        // actionBar
        ActionBar actionBar = getSupportActionBar();
        // Set below attributes to add logo in ActionBar.
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setTitle("Stocks");

        //create datetime
        calendar = Calendar.getInstance();
        dateTimeDisplay = (TextView)findViewById(R.id.date);
        dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        date = dateFormat.format(calendar.getTime());
        dateTimeDisplay.setText(date);

        // preference resousrce
        try {
            loadData();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView cbValue = (TextView) findViewById(R.id.cbValue);
        cbValue.setText("$" + Math.round(moneyInPocket * 100.0) / 100.0);
        netWorth = Math.round(moneyInPocket * 100.0) / 100.0;
        nwValue = (TextView) findViewById(R.id.nwValue);
        nwValue.setText("$" + netWorth);
        processBar.setVisibility(View.GONE);

        // favorite recycler view
        recyclerFavView = findViewById(R.id.recyclerFavorite);
        recyclerFavoriteAdaptor = new favoriteAdaptor(final_fav_data);
        recyclerFavView.setAdapter(recyclerFavoriteAdaptor);
        recyclerFavView.addItemDecoration(new DividerItemDecoration(recyclerFavView.getContext(), DividerItemDecoration.VERTICAL));
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerFavView);

        recyclerPortfolioView = findViewById(R.id.recyclerPortfolio);
        recyclerPortfolioAdaptor = new portfolioAdaptor(final_portfolio_data);
        recyclerPortfolioView.setAdapter(recyclerPortfolioAdaptor);
        recyclerPortfolioView.addItemDecoration(new DividerItemDecoration(recyclerPortfolioView.getContext(), DividerItemDecoration.VERTICAL));
        new ItemTouchHelper(portfolioCallBack).attachToRecyclerView(recyclerPortfolioView);

        // initialize dataset
        try {
            initializeData();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // footer
        footer = findViewById(R.id.footer);
//        footer.setMovementMethod(LinkMovementMethod.getInstance());
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                String url = "https://www.finnhub.io";
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume(){

        handler.postDelayed( runnable = new Runnable() {
            public void run() {
                try {
                    netWorth = Math.round(moneyInPocket * 100.0) / 100.0;
                    initializeData();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                handler.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
        if(shouldExecuteOnResume){
            try {
                loadData();
            } catch (JSONException e) {
                e.printStackTrace();
            }
                final_portfolio_data = new JSONArray();
                final_fav_data = new JSONArray();
                netWorth = Math.round(moneyInPocket * 100.0) / 100.0;
                try {
                    initializeData();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                recyclerFavoriteAdaptor.notifyDataSetChanged();
                recyclerPortfolioAdaptor.notifyDataSetChanged();


            TextView cbValue = (TextView) findViewById(R.id.cbValue);
            cbValue.setText("$" + Math.round(moneyInPocket * 100.0) / 100.0);
        } else{
            shouldExecuteOnResume = true;
        }

    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }

    public void saveData() {
        preferences= getSharedPreferences("preference", Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString("bought", boughtShares.toString());
        editor.putFloat("money", moneyInPocket);
        editor.putString("collect", favList.toString());
        editor.apply();
    }

    public void loadData() throws JSONException {
        preferences = getSharedPreferences("preference", Context.MODE_PRIVATE);
        boughtShares = new JSONArray(preferences.getString("bought", ""));
        moneyInPocket = preferences.getFloat("money", 0);
        favList = new JSONArray(preferences.getString("collect", ""));
    }

    public void initializeData() throws JSONException {
        // initialize dataset
        if (boughtShares.length() == 0) {
            while (final_portfolio_data.length() > 0) {
                final_portfolio_data.remove(0);
            }
            recyclerPortfolioAdaptor.updateData(final_portfolio_data);
            recyclerPortfolioAdaptor.notifyDataSetChanged();
        }

        if (favList.length() == 0) {
            while (final_fav_data.length() > 0) {
                final_fav_data.remove(0);
            }
            recyclerFavoriteAdaptor.updateData(final_fav_data);
            recyclerFavoriteAdaptor.notifyDataSetChanged();
        }
        final_portfolio_data = new JSONArray();
        for (int i = 0; i < boughtShares.length(); i++) {
            JSONObject curObj = new JSONObject();
            curObj.put("ticker", boughtShares.getJSONObject(i).getString("ticker"));
            final_portfolio_data.put(curObj);
            getLatestPriceData(boughtShares.getJSONObject(i).getString("ticker"));
        }

        final_fav_data = new JSONArray();
        for (int i = 0; i < favList.length(); i++) {
            JSONObject curObj = new JSONObject();
            curObj.put("ticker", favList.getString(i));
            final_fav_data.put(curObj);
            getLatestPriceForFav(favList.getString(i));
        }

    }


    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN
            | ItemTouchHelper.START | ItemTouchHelper.END, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPos = viewHolder.getBindingAdapterPosition();
            int toPos = target.getBindingAdapterPosition();
            JSONObject from_final = new JSONObject();
            String from_final_record = "";
            try {
                from_final = final_fav_data.getJSONObject(fromPos);
                from_final_record = favList.getString(fromPos);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                final_fav_data.put(fromPos, final_fav_data.getJSONObject(toPos));
                favList.put(fromPos, favList.getString(toPos));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                final_fav_data.put(toPos, from_final);
                favList.put(toPos, from_final_record);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            saveData();
            recyclerFavoriteAdaptor.notifyItemMoved(fromPos, toPos);
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder. getBindingAdapterPosition();
            final_fav_data.remove(position);
            favList.remove(position);
            saveData();
            recyclerFavoriteAdaptor.notifyDataSetChanged();
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.red))
                    .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    @Override
    @SuppressLint("RestrictedApi")
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.stocksSearch);


        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search...");

        searchAutoComplete = (SearchView.SearchAutoComplete)searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setBackgroundColor(getResources().getColor(R.color.white));
        searchAutoComplete.setTextColor(getResources().getColor(R.color.black));
        searchAutoComplete.setDropDownBackgroundResource(android.R.color.white);
        searchAutoComplete.setThreshold(1);

        newsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        searchAutoComplete.setAdapter(newsAdapter);


        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                String queryString=(String)adapterView.getItemAtPosition(itemIndex);
                String cur_ticker = "";
                int index = 0;
                while(!Character.isWhitespace(queryString.charAt(index))) {
                    cur_ticker += queryString.charAt(index);
                    index++;
                }
                searchAutoComplete.setText("" + cur_ticker);
                Intent intent = new Intent(MainActivity.this, Detail.class);
                intent.putExtra("ticker", cur_ticker);
                startActivity(intent);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(MainActivity.this, Detail.class);
                intent.putExtra("ticker", query.toUpperCase());
                startActivity(intent);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                getAutoC(newText);
                if (newText.length() == 0) {
                    newsAdapter.clear();
                }
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }


    ItemTouchHelper.SimpleCallback portfolioCallBack = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN
            | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPos = viewHolder.getBindingAdapterPosition();
            int toPos = target.getBindingAdapterPosition();
            JSONObject from_final = new JSONObject();
            JSONObject from_final_record = new JSONObject();
            try {
                from_final = final_portfolio_data.getJSONObject(fromPos);
                from_final_record = boughtShares.getJSONObject(fromPos);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                final_portfolio_data.put(fromPos, final_portfolio_data.getJSONObject(toPos));
                boughtShares.put(fromPos, boughtShares.getJSONObject(toPos));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                final_portfolio_data.put(toPos, from_final);
                boughtShares.put(toPos, from_final_record);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            saveData();
            recyclerPortfolioAdaptor.notifyItemMoved(fromPos, toPos);
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        }
    };

    public void getAutoC(String ticker) {
        StringRequest post = new StringRequest(Request.Method.GET, "https://hw-8-csci571.wl.r.appspot.com/autoC?ticker=" + ticker,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.Q)
                    @Override
                    public void onResponse(String response) {
                        try {
                            newsAdapter.clear();
                            JSONArray info = new JSONArray(response);
                            for (int i = 0; i < info.length(); i++) {
                                  newsAdapter.add(info.getJSONObject(i).getString("symbol") + info.getJSONObject(i).getString("description"));
                            }
                            newsAdapter.notifyDataSetChanged();
                            searchAutoComplete.refreshAutoCompleteResults();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(post);
    }

    public void getLatestPriceData(String ticker) {
        StringRequest post = new StringRequest(Request.Method.GET, "https://hw-8-csci571.wl.r.appspot.com/latestPrice?ticker=" + ticker,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject info = new JSONArray(response).getJSONObject(0);

                            for (int i = 0; i < final_portfolio_data.length(); i++) {
                                if (final_portfolio_data.getJSONObject(i).getString("ticker").equals(ticker)) {
                                    JSONObject newObject = new JSONObject();
                                    newObject.put("ticker", boughtShares.getJSONObject(i).getString("ticker"));
                                    newObject.put("share", boughtShares.getJSONObject(i).getInt("share"));
                                    Double price_now = Math.round(info.getDouble("c")* 100.0) / 100.0 * (boughtShares.getJSONObject(i).getInt("share"));
                                    newObject.put("market_value", Math.round(price_now * 100.0) / 100.0);
                                    netWorth += Math.round(price_now * 100.0) / 100.0;
                                    netWorth = Math.round(netWorth * 100.0) / 100.0;
                                    nwValue.setText("$" + netWorth);
                                    Double change = price_now - boughtShares.getJSONObject(i).getDouble("cur");
                                    newObject.put("change_in_price", Math.round(change * 100.0) / 100.0);
                                    Double change_percent = Math.round(change / boughtShares.getJSONObject(i).getDouble("cur") * 100.0) / 100.0 * 100;
                                    newObject.put("change_in_percent", change_percent);
                                    final_portfolio_data.put(i, newObject);
                                }
                            }

                            recyclerPortfolioAdaptor.updateData(final_portfolio_data);
                            recyclerPortfolioAdaptor.notifyDataSetChanged();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(post);
    }

    public void getLatestPriceForFav(String ticker) {

        StringRequest post = new StringRequest(Request.Method.GET, "https://hw-8-csci571.wl.r.appspot.com/latestPrice?ticker=" + ticker,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject info = new JSONArray(response).getJSONObject(0);
                            JSONObject item_info = new JSONObject();
                            for (int i = 0; i < final_fav_data.length(); i++) {
                                if (final_fav_data.getJSONObject(i).getString("ticker").equals(ticker)) {
                                    item_info.put("ticker", ticker);
                                    item_info.put("c", Math.round(info.getDouble("c")* 100.0) / 100.0);
                                    item_info.put("d", Math.round(info.getDouble("d")* 100.0) / 100.0);
                                    item_info.put("dp", Math.round(info.getDouble("dp")* 100.0) / 100.0);
                                    final_fav_data.put(i, item_info);
                                }
                            }
                            getProfileData(ticker);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(post);
    }

    public void getProfileData(String ticker) {
        StringRequest post = new StringRequest(Request.Method.GET, "https://hw-8-csci571.wl.r.appspot.com/posts?ticker=" + ticker,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject info = new JSONArray(response).getJSONObject(0);
                            for (int i = 0; i < final_fav_data.length(); i++) {
                                JSONObject cur_in_list = final_fav_data.getJSONObject(i);
                                if (cur_in_list.getString("ticker").equals(info.getString("ticker"))) {
                                    cur_in_list.put("name", info.getString("name"));
                                    final_fav_data.put(i, cur_in_list);
                                }
                            }
                            recyclerFavoriteAdaptor.updateData(final_fav_data);
                            recyclerFavoriteAdaptor.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(post);
    }
}