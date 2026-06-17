package com.nata.ai;

import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // UI Elements
    private TextView tvCoinBalance, tvAiResponse;
    private EditText etUserQuery;
    private ImageView imgAiPreview;
    private Spinner spinnerModels;
    
    // Logic Variables
    private RewardedAd rewardedAd;
    private RequestQueue requestQueue;
    private int userCoins = 50; // Saantima jalqabaa badhaasaa
    private String selectedModel = "Gemini";
    private final String SERVER_URL = "http://127.0.0.1:8000/v1/api/chat"; // Termux Localhost URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 1. FILTER ITIYOOPHIYAA: Bilbilli kun simcard Itiyoophiyaa qabaachuu mirkaneessuuf
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null && !"et".equalsIgnoreCase(tm.getNetworkCountryIso())) {
            Toast.makeText(this, "NATA AI works only in Ethiopia!", Toast.LENGTH_LONG).show();
            finish();
            System.exit(0);
        }

        setContentView(R.layout.activity_main);
        requestQueue = Volley.newRequestQueue(this);

        // UI Meeshaalee walitti hidhuu
        tvCoinBalance = findViewById(R.id.txt_coin_balance);
        tvAiResponse = findViewById(R.id.tv_ai_response);
        etUserQuery = findViewById(R.id.et_user_query);
        imgAiPreview = findViewById(R.id.img_ai_preview);
        spinnerModels = findViewById(R.id.spinner_models);
        ImageButton btnSubmit = findViewById(R.id.btn_submit_query);
        Button btnWatchAd = findViewById(R.id.btn_watch_ad);
        Button btnNewChat = findViewById(R.id.btn_new_chat);

        // Spinner (Dropdown) Moodela AI qopheessuu
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.ai_models_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModels.setAdapter(adapter);

        // AdMob Video Beeksisa fe'uu
        loadRewardedAd();

        // Button: New Chat (Fulaa qulqulleessuuf)
        btnNewChat.setOnClickListener(v -> {
            etUserQuery.setText("");
            tvAiResponse.setText("NATA AI: Ready for a new chat!");
            imgAiPreview.setVisibility(View.GONE);
        });

        // Button: Beeksisa Ilaaluu
        btnWatchAd.setOnClickListener(v -> showAdAndReward());

        // Button: AI Gaafadhu
        btnSubmit.setOnClickListener(v -> {
            String query = etUserQuery.getText().toString().trim();
            if (query.isEmpty()) return;

            // Gatii Koinii moodela irratti hundaa'e
            String item = spinnerModels.getSelectedItem().toString();
            int cost = 1;
            if (item.contains("ChatGPT")) { selectedModel = "ChatGPT"; cost = 2; }
            else if (item.contains("Claude")) { selectedModel = "Claude"; cost = 3; }
            else { selectedModel = "Gemini"; }

            if (userCoins >= cost) {
                userCoins -= cost;
                tvCoinBalance.setText("🪙 " + userCoins + " Coins");
                sendQueryToServer(query, selectedModel);
            } else {
                Toast.makeText(this, "Not enough coins! Watch an ad.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Google AdMob Video Rewarded Fe'uu
    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest,
            new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(RewardedAd ad) { rewardedAd = ad; }
                @Override
                public void onAdFailedToLoad(LoadAdError error) { rewardedAd = null; }
            });
    }

    // Koinii Badhaasuu (Ads Reward)
    private void showAdAndReward() {
        if (rewardedAd != null) {
            rewardedAd.show(this, rewardItem -> {
                userCoins += 5; // Koinii 5 badhaasa
                tvCoinBalance.setText("🪙 " + userCoins + " Coins");
                loadRewardedAd();
            });
        } else {
            Toast.makeText(this, "Ad is loading, try again in a few seconds!", Toast.LENGTH_SHORT).show();
            loadRewardedAd();
        }
    }

    // Volley HTTP Post: Ergaa gara Termux (Python Server) oofuuf
    private void sendQueryToServer(String query, String model) {
        tvAiResponse.setText("NATA AI is thinking...");
        JSONObject body = new JSONObject();
        try {
            body.put("query", query);
            body.put("model", model);
            body.put("user_id", "nata_user_01");
        } catch (JSONException e) { e.printStackTrace(); }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, SERVER_URL, body,
            res -> {
                try {
                    String type = res.getString("type");
                    tvAiResponse.setText(res.getString("response"));
                    
                    // Yoo deebiin suuraa uumuu ta'e ImageView argisiisi
                    if ("image".equals(type)) {
                        imgAiPreview.setVisibility(View.VISIBLE);
                        // Asirratti URL suuraa dhufu fulaa irratti fe'a
                    } else {
                        imgAiPreview.setVisibility(View.GONE);
                    }
                } catch (Exception e) { 
                    tvAiResponse.setText("NATA AI: Error processing response."); 
                }
            },
            err -> tvAiResponse.setText("Error: Server offline. Make sure Termux is running!")) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Content-Type", "application/json");
                return h;
            }
        };
        requestQueue.add(req);
    }
}
