package com.techotd.matrimo;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import com.techotd.matrimo.adapter.TemplateAdapter;
import com.techotd.matrimo.utils.UserData;
import com.techotd.matrimo.model.TemplateModel;

public class TemplateSelectActivity extends AppCompatActivity {

    private AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_select);

        // Back button to finish the activity
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        UserData userData = getIntent().getParcelableExtra("user_data");

        List<TemplateModel> templates = new ArrayList<>();
        templates.add(new TemplateModel("Template 1", R.drawable.samplehindutemplate, R.layout.hindutemplate1));
        templates.add(new TemplateModel("Template 2", R.drawable.samplemuslimtemplate, R.layout.muslimtemplate1));
        templates.add(new TemplateModel("Template 3", R.drawable.sampletemplate3, R.layout.template3));
        templates.add(new TemplateModel("Template 4", R.drawable.sampletemplate4, R.layout.template4));

        RecyclerView recyclerView = findViewById(R.id.template_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        TemplateAdapter adapter = new TemplateAdapter(this, templates, template -> {
            Intent intent = new Intent(TemplateSelectActivity.this, PreviewActivity.class);
            intent.putExtra("user_data", userData);
            intent.putExtra("template_id", template.getLayoutId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        initializeAdMob();
    }

    private void initializeAdMob() {
        // Initialize the Mobile Ads SDK
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d(TAG, "AdMob SDK initialized: " + initializationStatus.toString());
                // Load ad after initialization completes
                loadBannerAd();
            }
        });
    }

    private void loadBannerAd() {
        mAdView = findViewById(R.id.adView);

        // Create ad request
        AdRequest adRequest = new AdRequest.Builder().build();

        // Add listener for better debugging
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                Log.e(TAG, "Ad failed to load: " + loadAdError.getMessage() +
                        " (Code: " + loadAdError.getCode() + ")");
            }
        });

        // Load the ad directly without setting size
        mAdView.loadAd(adRequest);
    }


    // Handle AdView lifecycle
    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
}