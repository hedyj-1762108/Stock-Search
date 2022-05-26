package com.example.csci571_hw9;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Detail extends AppCompatActivity {

    private boolean collected = false;
    private String ticker = "";
    private Menu menu;
    private RecyclerView peers;
    private ArrayList<String> peerName;
    private LinearLayoutManager linearLayoutManager;
    private PeerAdapter peerAdapter;
    private WebView recomTrend;
    private WebView eps;
    // for hourly graph
    private long timeStamp = System.currentTimeMillis() / 1000;
    private String hourlyData;
    private ViewPager2 charts;
    private TabLayout tabLayout;
    private String color = "black";
    private ArrayList<double[]> alltimeData = new ArrayList();
    private ArrayList<Long> time = new ArrayList();
    private ArrayList<Long> volume = new ArrayList();
    private RecyclerView news_recy;
    private CardView first_news;

    //trade dialog
    private Button tradeButton;
    private String companyName;
    private double cur_price;

    //shared preference
    private JSONArray boughtShares = new JSONArray();
    private JSONArray collectList = new JSONArray();
    private float moneyInPocket = 25000;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    //procress bar
    private ProgressBar processBar;

    // handler
    private Handler handler = new Handler();
    private Runnable runnable;
    private int delay = 15*1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_CSCI571hw9);
        setContentView(R.layout.activity_detail);
        processBar = findViewById(R.id.processBar);
        processBar.setVisibility(View.VISIBLE);
        ticker = getIntent().getExtras().getString("ticker");
        this.setTitle(ticker);
        //go back icon
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        charts = findViewById(R.id.viewPager2);
        charts.setAdapter(new tabAdaptor(getSupportFragmentManager(), getLifecycle()));
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.purple_500));
        tabLayout.setTabTextColors(Color.parseColor("#000000"), Color.parseColor("#6200EE"));
        new TabLayoutMediator(tabLayout, charts, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position == 0) {
                    tab.setIcon(R.drawable.ic_baseline_show_chart_24);
                }
                if (position == 1) {
                    tab.setIcon(R.drawable.ic_baseline_access_time_24);
                }
            }
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setTint(getResources().getColor(R.color.purple_500));
                charts.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setTint(getResources().getColor(R.color.black));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        charts.setCurrentItem(0);
        getPeerData();
        getProfileData();
        getSentiment();
        //tabs
        //rec trend
        getRecData();
        // EPS
        getEPSData();
        // news
        getNewsData();
        //historical
        getALLData();

        // trade dialog
        tradeButton = (Button) findViewById(R.id.trade);

        // update collect
        try {
            loadData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < collectList.length(); i++) {
            try {
                if (collectList.getString(i).equals(ticker)) {
                    collected = true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        //start handler as activity become visible
        handler.postDelayed( runnable = new Runnable() {
            public void run() {
                getProfileData();
                handler.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
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
        editor.putString("collect", collectList.toString());
        editor.apply();
    }

    public void loadData() throws JSONException {
        preferences = getSharedPreferences("preference", Context.MODE_PRIVATE);
        boughtShares = new JSONArray(preferences.getString("bought", ""));
        collectList = new JSONArray(preferences.getString("collect", ""));
        moneyInPocket = preferences.getFloat("money", 0);
    }



    public void getNewsData() {
        StringRequest post = new StringRequest(Request.Method.GET, "https://updatedapi-hanjinji.wl.r.appspot.com/news?ticker=" + ticker,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray info = new JSONArray(response);
                            JSONObject firstNews = info.getJSONObject(0);
                            // first news
                            long first_datetime_stamp = firstNews.getLong("datetime");
                            Date first_datetime = new Date(first_datetime_stamp);
                            Date now = new Date();
//                            long first_diff = (now.getTime() - first_datetime.getTime()) / (60 * 60 * 1000);
                            long first_diff = now.getTime() - first_datetime.getTime();
                            TextView first_hour_view = (TextView) findViewById(R.id.first_news_time);
                            if (((now.getTime() - first_datetime.getTime()) / 1000) < 60) {
                                first_diff = first_diff / 1000;
                                first_hour_view.setText(first_diff + " seconds ago");
                            } else if (((now.getTime() - first_datetime.getTime()) / (60 * 1000)) < 60) {
                                first_diff = first_diff / (60 * 1000);
                                first_hour_view.setText(first_diff + " minutes ago");
                            } else {
                                first_diff = (now.getTime() - first_datetime.getTime()) / (60 * 60 * 1000);
                                first_hour_view.setText(first_diff + " hours ago");
                            }
                            String first_headline = firstNews.getString("headline");
                            String first_weblink = firstNews.getString("url");
                            String first_source = firstNews.getString("source");
                            TextView first_headline_view = (TextView) findViewById(R.id.firstHeadLine);
                            first_headline_view.setText(first_headline);
                            TextView first_source_view = (TextView) findViewById(R.id.first_news_resource);
                            first_source_view.setText(first_source);
                            ImageView first_newsimage = (ImageView) findViewById(R.id.firstNewsImage);
                            Picasso.with(getBaseContext()).load(firstNews.getString("image")).into(first_newsimage);
                            String[] monthNames = new String[]{"January", "February", "March", "April", "May", "June",
                                    "July", "August", "September", "October", "November", "December"};
                            Calendar calendar = new GregorianCalendar();
                            calendar.setTime(first_datetime);
                            int year = calendar.get(Calendar.YEAR);
                            String month = monthNames[calendar.get(Calendar.MONTH)];
                            int day = calendar.get(Calendar.DAY_OF_MONTH);
                            String time_string = month + " " + day + ", " + year;
                            String first_summary = firstNews.getString("summary");
                            // for recyclerview
                            String[] resource_info = new String[info.length() - 1];
                            String[] headline_info = new String[info.length() - 1];
                            String[] time_info = new String[info.length() - 1];
                            String[] img_info = new String[info.length() - 1];
                            String[] url_info = new String[info.length() - 1];
                            String[] summary_info = new String[info.length() - 1];
                            String[] timeread_info = new String[info.length() - 1];
                            for (int i = 0; i < info.length() - 1; i++) {
                                resource_info[i] = info.getJSONObject(i + 1).getString("source");
                                headline_info[i] = info.getJSONObject(i + 1).getString("headline");
                                long datetime_stamp = info.getJSONObject(i + 1).getLong("datetime");
                                Date datetime = new Date(datetime_stamp);
//                                long diff = (now.getTime() - datetime.getTime()) / (60 * 60 * 1000);
//                                time_info[i] = diff + " hours ago";
                                long diff = now.getTime() - datetime.getTime();
                                if ((diff / 1000) < 60) {
                                    diff = diff / 1000;
                                    time_info[i] = diff + " seconds ago";
                                } else if ((diff / (1000 * 60)) < 60) {
                                    diff = diff / (60 * 1000);
                                    time_info[i] = diff + " minutes ago";
                                } else {
                                    diff = diff / (60 * 60 * 1000);
                                    time_info[i] = diff + " hours ago";
                                }
                                img_info[i] = info.getJSONObject(i + 1).getString("image");
                                url_info[i] = info.getJSONObject(i + 1).getString("url");
                                summary_info[i] = info.getJSONObject(i + 1).getString("summary");

                                long stamp = info.getJSONObject(i + 1).getLong("datetime");
                                Date datetime_format = new Date(stamp);
                                calendar = new GregorianCalendar();
                                calendar.setTime(datetime_format);
                                year = calendar.get(Calendar.YEAR);
                                month = monthNames[calendar.get(Calendar.MONTH)];
                                day = calendar.get(Calendar.DAY_OF_MONTH);
                                timeread_info[i] = month + " " + day + ", " + year;
                            }
                            news_recy = findViewById(R.id.newsRecy);
                            newsAdapter myNewsAdaptor = new newsAdapter(resource_info, headline_info, time_info, img_info, url_info, summary_info, timeread_info);
                            news_recy.setAdapter(myNewsAdaptor);
                            news_recy.setLayoutManager(new LinearLayoutManager(Detail.this));

                            // dialog
                            first_news = findViewById(R.id.cardView);
                            first_news.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    openNewsDialog(first_source, time_string, first_headline, first_summary, first_weblink);
                                }
                            });

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

    public void openNewsDialog(String title, String date, String headline, String summary, String url) {
        newsDialog newsDialog = new newsDialog().newInstance(title, date, headline, summary, url);
        newsDialog.show(getSupportFragmentManager(), "news dialog");
    }

    public void getALLData() {
        StringRequest post = new StringRequest(Request.Method.GET, "https://hw-8-csci571.wl.r.appspot.com/chart?ticker=" + ticker,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject info = new JSONObject(response);
                            for (int i = 0; i < info.getJSONArray("c").length(); i++) {
                                double[] point = new double[4];
                                time.add(info.getJSONArray("t").getLong(i));
                                volume.add(info.getJSONArray("v").getLong(i));
                                point[0] = info.getJSONArray("o").getDouble(i);
                                point[1] = info.getJSONArray("h").getDouble(i);
                                point[2] = info.getJSONArray("l").getDouble(i);
                                point[3] = info.getJSONArray("c").getDouble(i);
                                alltimeData.add(point);
                            }
                            charts.setAdapter(new tabAdaptor(getSupportFragmentManager(), getLifecycle()));
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

    public void getHourlyData() {
        StringRequest post = new StringRequest(Request.Method.GET, "https://hw-8-csci571.wl.r.appspot.com/sixHourPrice?ticker=" + ticker + "&to=" + timeStamp,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray info = new JSONArray(response);
                            hourlyData = info.toString();
                            charts.setAdapter(new tabAdaptor(getSupportFragmentManager(), getLifecycle()));
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

    public void getEPSData() {
        StringRequest post = new StringRequest(Request.Method.GET, "https://hw-8-csci571.wl.r.appspot.com/earn?ticker=" + ticker,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray info = new JSONArray(response);
                            Double[] act = new Double[4];
                            Double[] est = new Double[4];
                            String[] x = new String[4];
                            Double[] surp = new Double[4];
                            for (int i = 0; i < info.length(); i++) {
                                act[i] = info.getJSONObject(i).getDouble("actual");
                                est[i] = info.getJSONObject(i).getDouble("estimate");
                                x[i] = info.getJSONObject(i).getString("period");
                                surp[i] = info.getJSONObject(i).getDouble("surprise");
                            }
                            eps = (WebView) findViewById(R.id.eps);
                            eps.getSettings().setJavaScriptEnabled(true);
                            eps.setWebViewClient(new WebViewClient() {
                            });
                            eps.setWebViewClient(new WebViewClient() {
                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    super.onPageFinished(view, url);
                                    String val_act = Arrays.toString(act);
                                    String val_est = Arrays.toString(est);
                                    String val_surp = Arrays.toString(surp);
                                    String data = "[" + val_act + "," + val_est + "," + val_surp + "]";
                                    eps.loadUrl("javascript:loadEarnData(\"" + data + "\",\"" + x[0] + "\",\"" + x[1] + "\",\"" + x[2] + "\",\"" + x[3] + "\")");
                                }
                            });
                            eps.loadUrl("file:///android_asset/eps.html");
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

    public void getRecData() {
        StringRequest post = new StringRequest(Request.Method.GET, "https://hw-8-csci571.wl.r.appspot.com/rec?ticker=" + ticker,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray info = new JSONArray(response);
                            int[] strong_buy = new int[4];
                            int[] buy = new int[4];
                            int[] hold = new int[4];
                            int[] sell = new int[4];
                            int[] strong_sel = new int[4];
                            String[] x = new String[4];
                            for (int i = 0; i < info.length(); i++) {
                                strong_buy[i] = info.getJSONObject(i).getInt("strongBuy");
                                buy[i] = info.getJSONObject(i).getInt("buy");
                                hold[i] = info.getJSONObject(i).getInt("hold");
                                sell[i] = info.getJSONObject(i).getInt("sell");
                                strong_sel[i] = info.getJSONObject(i).getInt("strongSell");
                                x[i] = info.getJSONObject(i).getString("period");
                            }
                            recomTrend = (WebView) findViewById(R.id.recomTrend);
                            recomTrend.getSettings().setJavaScriptEnabled(true);
                            recomTrend.setWebViewClient(new WebViewClient() {
                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    super.onPageFinished(view, url);
                                    String val = Arrays.toString(strong_buy);
                                    String val_buy = Arrays.toString(buy);
                                    String val_hold = Arrays.toString(hold);
                                    String val_sell = Arrays.toString(sell);
                                    String val_strong_sell = Arrays.toString(strong_sel);
                                    String data = "[" + val + "," + val_buy + "," + val_hold + ","
                                            + val_sell + "," + val_strong_sell + "]";
                                    recomTrend.loadUrl("javascript:loadData(\"" + data + "\",\"" + x[0] + "\",\"" + x[1] + "\",\"" + x[2] + "\",\"" + x[3] + "\")");
                                }
                            });
                            recomTrend.loadUrl("file:///android_asset/rec.html");
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

    public void getPeerData() {
        StringRequest post = new StringRequest(Request.Method.GET, "https://hw-8-csci571.wl.r.appspot.com/peer?ticker=" + ticker,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray info = new JSONArray(response);
                            peerName = new ArrayList<>();
                            for (int i = 0; i < info.length(); i++) {
                                peerName.add(info.getString(i));
                            }
                            // peer recycler
                            peers = findViewById(R.id.peers);
                            linearLayoutManager = new LinearLayoutManager(Detail.this, LinearLayoutManager.HORIZONTAL, false);
                            peerAdapter = new PeerAdapter(peerName);
                            peers.setLayoutManager(linearLayoutManager);
                            peers.setAdapter(peerAdapter);

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

    public void getProfileData() {
        StringRequest post = new StringRequest(Request.Method.GET, "https://hw-8-csci571.wl.r.appspot.com/posts?ticker=" + ticker,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject info = new JSONArray(response).getJSONObject(0);
                            TextView ticker = (TextView) findViewById(R.id.ticker);
                            ticker.setText(info.getString("ticker"));
                            TextView name = (TextView) findViewById(R.id.name);
                            companyName = info.getString("name");
                            name.setText(info.getString("name"));
                            ImageView imageView = (ImageView) findViewById(R.id.logo);
                            Picasso.with(getBaseContext()).load(info.getString("logo")).into(imageView);

                            TextView IPO = (TextView) findViewById(R.id.startDate2);
                            String raw_ipo = info.getString("ipo");
                            String ipo_year = raw_ipo.substring(0, 4);
                            String ipo_date= raw_ipo.substring(8, 10);
                            String ipo_month= raw_ipo.substring(5, 7);
                            IPO.setText(ipo_month + " -" + ipo_date + " -" + ipo_year);
                            TextView industry = (TextView) findViewById(R.id.industryValue);
                            industry.setText(info.getString("finnhubIndustry"));

                            TextView web = (TextView) findViewById(R.id.webPageValue);
                            web.setText(info.getString("weburl"));
                            web.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                    String url = null;
                                    try {
                                        url = info.getString("weburl");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    intent.setData(Uri.parse(url));
                                    startActivity(intent);
                                }
                            });
                            web.setTextColor(getResources().getColor(R.color.purple_500));
                            web.getPaint().setUnderlineText(true);
                            // sentiment table
                            TextView companyName = (TextView) findViewById(R.id.companyName);
                            companyName.setText(info.getString("name"));
                            processBar.setVisibility(View.GONE);
                            getLatestPriceData();
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

    public void getSentiment() {
        StringRequest post = new StringRequest(Request.Method.GET, "https://hw-8-csci571.wl.r.appspot.com/sentiment?ticker=" + ticker,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject info = new JSONObject(response);
                            JSONArray reddit = info.getJSONArray("reddit");
                            JSONArray twitter = info.getJSONArray("twitter");
                            int reddit_mention = 0;
                            int reddit_pos_mention = 0;
                            int reddit_neg_mention = 0;
                            int twitter_mention = 0;
                            int twitter_pos_mention = 0;
                            int twitter_neg_mention = 0;
                            for (int i = 0; i < reddit.length(); i++) {
                                reddit_mention += Integer.valueOf(reddit.getJSONObject(i).getString("mention"));
                                reddit_pos_mention += Integer.valueOf(reddit.getJSONObject(i).getString("positiveMention"));
                                reddit_neg_mention += Integer.valueOf(reddit.getJSONObject(i).getString("negativeMention"));
                            }
                            for (int i = 0; i < twitter.length(); i++) {
                                twitter_mention += Integer.valueOf(twitter.getJSONObject(i).getString("mention"));
                                twitter_pos_mention += Integer.valueOf(twitter.getJSONObject(i).getString("positiveMention"));
                                twitter_neg_mention += Integer.valueOf(twitter.getJSONObject(i).getString("negativeMention"));
                            }
                            TextView red_t = (TextView) findViewById(R.id.redditTotal);
                            red_t.setText("" + reddit_mention);
                            TextView red_pos = (TextView) findViewById(R.id.redditPos);
                            red_pos.setText("" + reddit_pos_mention);
                            TextView red_neg = (TextView) findViewById(R.id.redditNeg);
                            red_neg.setText("" + reddit_neg_mention);

                            TextView twi_t = (TextView) findViewById(R.id.twitterTotal);
                            twi_t.setText("" + twitter_mention);
                            TextView twi_pos = (TextView) findViewById(R.id.twitterPos);
                            twi_pos.setText("" + twitter_pos_mention);
                            TextView twi_neg = (TextView) findViewById(R.id.twitterNeg);
                            twi_neg.setText("" + twitter_neg_mention);

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

    public void getLatestPriceData() {
        StringRequest post = new StringRequest(Request.Method.GET, "https://hw-8-csci571.wl.r.appspot.com/latestPrice?ticker=" + ticker,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject info = new JSONArray(response).getJSONObject(0);
                            TextView price = (TextView) findViewById(R.id.price);
                            Double priceNum = Double.valueOf(info.getString("c"));
                            cur_price = Math.round(priceNum * 100.0) / 100.0;
                            price.setText("$" + Math.round(priceNum * 100.0) / 100.0);
                            TextView change = (TextView) findViewById(R.id.priceChange);
                            // portfolio Section
                            Double percentage = Double.valueOf(info.getString("dp"));
                            ImageView up = (ImageView) findViewById(R.id.imageView);
                            ImageView down = (ImageView) findViewById(R.id.imageViewDown);
                            timeStamp = info.getLong("t");

                            updatePortfolioSection(priceNum);

                            // stats section
                            TextView openP = (TextView) findViewById(R.id.opV);
                            TextView lowP = (TextView) findViewById(R.id.lpV);
                            TextView highP = (TextView) findViewById(R.id.hpV);
                            TextView prevP = (TextView) findViewById(R.id.ppV);
                            if (percentage < 0) {
                                color = "red";
                                change.setTextColor(getResources().getColor(R.color.red));
                                up.setVisibility(up.GONE);
                                down.setVisibility(down.VISIBLE);
                            } else if (percentage > 0) {
                                color = "green";
                                change.setTextColor(getResources().getColor(R.color.green));

                                up.setVisibility(up.VISIBLE);
                                down.setVisibility(down.GONE);
                            }
                            Double changeNum = Double.valueOf(info.getString("d"));
                            change.setText("$" + Math.round(changeNum * 100.0) / 100.0 + " (" + Math.round(percentage * 100.0) / 100.0 + "%)");

                            // stats section
                            openP.setText("$" + Math.round(Double.valueOf(info.getString("o")) * 100.0) / 100.0);
                            lowP.setText("$" + Math.round(Double.valueOf(info.getString("l")) * 100.0) / 100.0);
                            highP.setText("$" + Math.round(Double.valueOf(info.getString("h")) * 100.0) / 100.0);
                            prevP.setText("$" + Math.round(Double.valueOf(info.getString("pc")) * 100.0) / 100.0);
                            // get hourly graph
                            getHourlyData();
                            // get all graph

                            // get trade dialog
                            tradeButton.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    openTradeDialog(companyName, ticker.toUpperCase(), cur_price);
                                }
                            });
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

    public void updatePortfolioSection(double priceNum) throws JSONException {
        // portfolio section
        loadData();
        JSONObject item;
        TextView shareOwned = (TextView) findViewById(R.id.shareNum);
        TextView aveCostValue = (TextView) findViewById(R.id.aveCostValue);
        TextView totalCostValue = (TextView) findViewById(R.id.totalCostValue);
        TextView changeValue = (TextView) findViewById(R.id.changeValue);
        TextView marketValue = (TextView) findViewById(R.id.mkvalueValue);
        boolean found_for_portfolio = false;
        for (int i = 0; i < boughtShares.length(); i++) {
            item = boughtShares.getJSONObject(i);
            String cur_ticker = item.getString("ticker");
            if (ticker.equals(cur_ticker)) {
                int cur_share = item.getInt("share");
                double all_value = item.getDouble("cur");
                if ((priceNum > Math.round(all_value / cur_share * 100.0) / 100.0)) {
                    changeValue.setTextColor(getResources().getColor(R.color.green));
                    marketValue.setTextColor(getResources().getColor(R.color.green));
                } else if ((priceNum < Math.round(all_value / cur_share * 100.0) / 100.0)) {
                    changeValue.setTextColor(getResources().getColor(R.color.red));
                    marketValue.setTextColor(getResources().getColor(R.color.red));
                }
                shareOwned.setText("" + cur_share);
                aveCostValue.setText("$" + Math.round(all_value / cur_share * 100.0) / 100.0);
                totalCostValue.setText("$" + Math.round(all_value * 100.0) / 100.0);
                changeValue.setText("$" + Math.round((priceNum - all_value / cur_share)* 100.0) / 100.0);
                marketValue.setText("$" + Math.round(cur_share * priceNum * 100.0) / 100.0);

                found_for_portfolio = true;
            }
        }
        if (found_for_portfolio == false) {
            changeValue.setTextColor(getResources().getColor(R.color.black));
            marketValue.setTextColor(getResources().getColor(R.color.black));
            shareOwned.setText("0");
            aveCostValue.setText("$0.00");
            totalCostValue.setText("$0.00");
            changeValue.setText("$0.00");
            marketValue.setText("$0.00");
        }
    }

    public void openTradeDialog(String companyName, String ticker, double cur_price) {
        Dialog the_tradeDialog = new Dialog(this);
        the_tradeDialog.setContentView(R.layout.tradedialog);
        the_tradeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tradeTitle = the_tradeDialog.findViewById(R.id.tradeTitle);
        tradeTitle.setText(companyName + " shares");
        TextView numOfPrice = the_tradeDialog.findViewById(R.id.numOfPrice);
        numOfPrice.setText("" + cur_price);
        TextView tradeTicker = the_tradeDialog.findViewById(R.id.tradeTicker);
        tradeTicker.setText(ticker);
        TextView total = the_tradeDialog.findViewById(R.id.finalPrice);
        EditText editText = (EditText) the_tradeDialog.findViewById(R.id.input);
        TextView numOfShare = the_tradeDialog.findViewById(R.id.numOfShare);
        editText.addTextChangedListener(new TextWatcher() {

                                            public void afterTextChanged(Editable s) {
                                                String value = editText.getText().toString();
                                                if (isInt(value) && Integer.parseInt(value) > 0) {
                                                    int input_share = Integer.parseInt(value);
                                                    numOfShare.setText("" + input_share);
                                                    double totalValue = Math.round(input_share * cur_price * 100.0) / 100.0;
                                                    total.setText("" + totalValue);

                                                } else {
                                                    total.setText("0.00");
                                                    numOfShare.setText("0");
                                                }
                                            }

                                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                            }

                                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                            }
                                        });

        Button buyButton = (Button) the_tradeDialog.findViewById(R.id.buttonForBuy);
        Button sellButton = (Button) the_tradeDialog.findViewById(R.id.sellButton);
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean found = false;
                if (isInt(editText.getText().toString()) && Integer.parseInt(editText.getText().toString()) >= 0) {
                    try {
                        loadData();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                        if (Integer.parseInt(editText.getText().toString()) * cur_price > moneyInPocket) {
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toastLayout));
                            TextView toastMessage = layout.findViewById(R.id.toastMessage);
                            toastMessage.setText("Not enough money to buy");
                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.BOTTOM, 0, 40);
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.setView(layout);
                            toast.show();
                        } else {
                            JSONObject item = new JSONObject();
                        for (int i = 0; i < boughtShares.length(); i++) {
                            try {
                                item = boughtShares.getJSONObject(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String cur_ticker = null;
                            try {
                                cur_ticker = item.getString("ticker");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (ticker.equals(cur_ticker)) {
                                double cur_price_value = 0;
                                try {
                                    cur_price_value = item.getDouble("cur");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                int cur_share = 0;
                                try {
                                    cur_share = item.getInt("share");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                cur_price_value += Math.round(Integer.parseInt(editText.getText().toString()) * cur_price * 100.0) / 100.0;
                                cur_share += Integer.parseInt(editText.getText().toString());
                                boughtShares.remove(i);
                                JSONObject jsonObj = new JSONObject();
                                try {
                                    jsonObj.put("ticker", ticker);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    jsonObj.put("cur", cur_price_value);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    jsonObj.put("share", cur_share);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                boughtShares.put(jsonObj);
                                moneyInPocket = (float) (moneyInPocket - Math.round(Integer.parseInt(editText.getText().toString()) * cur_price * 100.0) / 100.0);
                                saveData();
                                found = true;

                            }
                        }

                    if (found == false) {
                        JSONObject jsonObj = new JSONObject();
                        try {
                            jsonObj.put("ticker", ticker);
                            jsonObj.put("cur", Math.round(Integer.parseInt(editText.getText().toString()) * cur_price * 100.0) / 100.0);
                            jsonObj.put("share", Integer.parseInt(editText.getText().toString()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        boughtShares.put(jsonObj);
                        moneyInPocket = (float) (moneyInPocket - Math.round(Integer.parseInt(editText.getText().toString()) * cur_price * 100.0) / 100.0);
                        saveData();
                    }
                    try {
                        updatePortfolioSection(cur_price);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    openBuyDialog(editText.getText().toString());
                    the_tradeDialog.dismiss();
                    }
                } else {
                    if (!isInt(editText.getText().toString())) {
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toastLayout));
                        TextView toastMessage = layout.findViewById(R.id.toastMessage);
                        toastMessage.setText("Please enter a valid amount");
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.BOTTOM, 0, 40);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    } else if (Integer.parseInt(editText.getText().toString()) <= 0) {
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toastLayout));
                        TextView toastMessage = layout.findViewById(R.id.toastMessage);
                        toastMessage.setText("Cannot buy non-positive shares");
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.BOTTOM, 0, 40);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    }
                }
            }
        });

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isInt(editText.getText().toString())) {
                    Log.d("errorData", "enter valid amount");
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toastLayout));
                    TextView toastMessage = layout.findViewById(R.id.toastMessage);
                    toastMessage.setText("Please enter a valid amount");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 40);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                } else if (Integer.parseInt(editText.getText().toString()) <= 0) {
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toastLayout));
                    TextView toastMessage = layout.findViewById(R.id.toastMessage);
                    toastMessage.setText("Cannot sell non-positive shares");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 40);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                } else {
                    try {
                        loadData();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JSONObject item = new JSONObject();
                boolean found = false;
                for (int i = 0; i < boughtShares.length(); i++) {
                    try {
                        item = boughtShares.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String cur_ticker = null;
                    try {
                        cur_ticker = item.getString("ticker");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (ticker.equals(cur_ticker)) {
                        found = true;
                        int cur_share = 0;
                        try {
                            cur_share = item.getInt("share");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (cur_share >= Integer.parseInt(editText.getText().toString())) {
                            double cur_price_value = 0;
                            try {
                                cur_price_value = item.getDouble("cur");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            cur_price_value -= Math.round(Integer.parseInt(editText.getText().toString()) * cur_price * 100.0) / 100.0;
                            cur_share -= Integer.parseInt(editText.getText().toString());
                            boughtShares.remove(i);
                            if (cur_share > 0) {
                                JSONObject jsonObj = new JSONObject();
                                try {
                                    jsonObj.put("ticker", ticker);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    jsonObj.put("cur", cur_price_value);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    jsonObj.put("share", cur_share);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                boughtShares.put(jsonObj);
                            }
                            moneyInPocket = (float) (moneyInPocket + Math.round(Integer.parseInt(editText.getText().toString()) * cur_price * 100.0) / 100.0);
                            saveData();
                            try {
                                updatePortfolioSection(cur_price);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            the_tradeDialog.dismiss();
                            openSellDialog(editText.getText().toString());
                        } else {
                            LayoutInflater inflater = getLayoutInflater();
                            View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toastLayout));
                            TextView toastMessage = layout.findViewById(R.id.toastMessage);
                            toastMessage.setText("Not enough shares to sell");
                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.BOTTOM, 0, 40);
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.setView(layout);
                            toast.show();
                        }

                    }
                }
                if (found == false) {
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toastLayout));
                    TextView toastMessage = layout.findViewById(R.id.toastMessage);
                    toastMessage.setText("Not enough shares to sell");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 40);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }
             }
            }
        });
        the_tradeDialog.show();
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

    public void openBuyDialog(String share) {
        Dialog buyDialog = new Dialog(this);
        buyDialog.setContentView(R.layout.buydialog);
        buyDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView firstLine = (TextView) buyDialog.findViewById(R.id.firstline);
        firstLine.setText("You have successfully bought " + share);

        TextView secondLine = (TextView) buyDialog.findViewById(R.id.secondLine);
        secondLine.setText("shares of " + ticker.toUpperCase());

        Button doneutton = (Button) buyDialog.findViewById(R.id.done);
        doneutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyDialog.dismiss();
            }
        });

        buyDialog.show();
    }

    public void openSellDialog(String share) {
        Dialog buyDialog = new Dialog(this);
        buyDialog.setContentView(R.layout.buydialog);
        buyDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView firstLine = (TextView) buyDialog.findViewById(R.id.firstline);
        firstLine.setText("You have successfully sold " + share);

        TextView secondLine = (TextView) buyDialog.findViewById(R.id.secondLine);
        secondLine.setText("shares of " + ticker.toUpperCase());

        Button doneutton = (Button) buyDialog.findViewById(R.id.done);
        doneutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyDialog.dismiss();
            }
        });

        buyDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.infomenu, menu);
        this.menu = menu;
        MenuItem MenuItem = menu.findItem(R.id.collection);
        if (collected) {
            MenuItem.setIcon(R.drawable.ic_baseline_star_24);
        } else {
            MenuItem.setIcon(R.drawable.ic_baseline_star_outline_24);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        MenuItem MenuItem = menu.findItem(R.id.collection);
        switch (item.getItemId()) {
            case R.id.collection:
                if (collected) {
                    MenuItem.setIcon(R.drawable.ic_baseline_star_outline_24);
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.toast, (ViewGroup)findViewById(R.id.toastLayout));
                    TextView toastMessage = layout.findViewById(R.id.toastMessage);
                    toastMessage.setText(ticker + " is removed from favorites");
                    Toast toast= new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 40);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                    collected = false;
                    for (int i = 0; i < collectList.length(); i++) {
                        try {
                            if (collectList.getString(i).equals(ticker)) {
                                collectList.remove(i);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    saveData();
                    try {
                        loadData();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("collect", collectList.toString());
                } else {
                    MenuItem.setIcon(R.drawable.ic_baseline_star_24);
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.toast, (ViewGroup)findViewById(R.id.toastLayout));
                    TextView toastMessage = layout.findViewById(R.id.toastMessage);
                    toastMessage.setText(ticker + " is added to favorites");
                    Toast toast= new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 40);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                    collected = true;
                    collectList.put(ticker);
//                    collectList = new JSONArray();
                    saveData();
                    try {
                        loadData();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("collectnot", collectList.toString());
                }
                return true;
            default:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    class tabAdaptor extends FragmentStateAdapter {

        public tabAdaptor(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecyle) {

            super(fragmentManager, lifecyle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment new_F = null;
            if (position == 0) {
                new_F = new hourlyPrice(hourlyData, ticker, color);
            }
            ;
            if (position == 1) {
                new_F = new allTimePrice(alltimeData, time, volume, ticker.toUpperCase());
            }
            ;
            return new_F;
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    class PeerAdapter extends RecyclerView.Adapter<PeerAdapter.MyHolder> {

        ArrayList<String> data;

        public PeerAdapter(ArrayList<String> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(Detail.this).inflate(R.layout.each_peer, parent, false);
            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, int position) {
            holder.peerName.setText(data.get(position) + ",");
            holder.peerName.setTextColor(getResources().getColor(R.color.purple_500));
            holder.peerName.getPaint().setUnderlineText(true);
            String ticker = data.get(position);
            holder.peerName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //goes to new activity passing the item name
                    Intent intent = new Intent(Detail.this, Detail.class);
                    intent.putExtra("ticker", ticker);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class MyHolder extends RecyclerView.ViewHolder {
            TextView peerName;

            public MyHolder(@NonNull View itemView) {
                super(itemView);
                peerName = itemView.findViewById(R.id.peerName);
            }
        }
    }

    class newsAdapter extends RecyclerView.Adapter<newsAdapter.MyViewHolder> {

        String resource[], time[], img[], headLine[], url[], timeread[], summary[];


        public newsAdapter(String resource[], String headLine[], String time[], String img[], String url[], String summary[], String timeread[]) {
            this.resource = resource;
            this.time = time;
            this.img = img;
            this.headLine = headLine;
            this.url = url;
            this.summary = summary;
            this.timeread = timeread;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView recyResource, recyTime, recyHeadLine;
            ImageView newsImg;
            CardView each_news;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                this.recyResource = itemView.findViewById(R.id.recyResource);
                this.recyTime = itemView.findViewById(R.id.recyTime);
                this.recyHeadLine = itemView.findViewById(R.id.recyHeadLine);
                this.newsImg = itemView.findViewById(R.id.NewsImage);
                this.each_news = itemView.findViewById(R.id.each_news);
            }
        }

        @NonNull
        @Override
        public newsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(Detail.this);
            View view = inflater.inflate(R.layout.each_news, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull newsAdapter.MyViewHolder holder, int position) {
            holder.recyResource.setText(resource[position]);
            holder.recyTime.setText(time[position]);
            holder.recyHeadLine.setText(headLine[position]);
            Picasso.with(getBaseContext()).load(img[position]).into(holder.newsImg);

            String cur_resource = resource[position];
            String cur_headline = headLine[position];
            String cur_url = url[position];
            String cur_timeread = timeread[position];
            String cur_sum = summary[position];
            holder.each_news.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openNewsDialog(cur_resource, cur_timeread, cur_headline, cur_sum, cur_url);
                }
            });

        }

        @Override
        public int getItemCount() {
            return resource.length;
        }
    }
}