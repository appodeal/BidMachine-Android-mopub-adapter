package io.bidmachine.examples;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.BidMachineAdapterConfiguration;
import com.mopub.mobileads.BidMachineMediationSettings;
import com.mopub.mobileads.BidMachineUtils;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubRewardedVideoListener;
import com.mopub.mobileads.MoPubRewardedVideoManager;
import com.mopub.mobileads.MoPubRewardedVideos;
import com.mopub.mobileads.MoPubView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.bidmachine.AdRequest;
import io.bidmachine.BidMachine;
import io.bidmachine.BidMachineFetcher;
import io.bidmachine.banner.BannerRequest;
import io.bidmachine.banner.BannerSize;
import io.bidmachine.interstitial.InterstitialRequest;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.rewarded.RewardedRequest;
import io.bidmachine.utils.BMError;

public class BidMachineMoPubFetchActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static final String AD_UNIT_ID = "4068bca9a3a44977917d68338b75df64";
    private static final String BANNER_KEY = "4068bca9a3a44977917d68338b75df64";
    private static final String INTERSTITIAL_KEY = "6173ac5e48de4a8b9741571f93d9c04e";
    private static final String REWARDED_KEY = "e746b899b7d54a5d980d627626422c25";

    private Button btnLoadBanner;
    private Button btnShowBanner;
    private Button btnLoadInterstitial;
    private Button btnShowInterstitial;
    private Button btnLoadRewardedVideo;
    private Button btnShowRewardedVideo;
    private FrameLayout bannerContainer;

    private MoPubView moPubView;
    private MoPubInterstitial moPubInterstitial;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fetch);

        bannerContainer = findViewById(R.id.banner_container);
        btnLoadBanner = findViewById(R.id.load_banner);
        btnLoadBanner.setOnClickListener(v -> loadBanner());
        btnShowBanner = findViewById(R.id.show_banner);
        btnShowBanner.setOnClickListener(v -> showBanner());
        btnLoadInterstitial = findViewById(R.id.load_interstitial);
        btnLoadInterstitial.setOnClickListener(v -> loadInterstitial());
        btnShowInterstitial = findViewById(R.id.show_interstitial);
        btnShowInterstitial.setOnClickListener(v -> showInterstitial());
        btnLoadRewardedVideo = findViewById(R.id.load_rvideo);
        btnLoadRewardedVideo.setOnClickListener(v -> loadRewardedVideo());
        btnShowRewardedVideo = findViewById(R.id.show_rvideo);
        btnShowRewardedVideo.setOnClickListener(v -> showRewardedVideo());
        findViewById(R.id.btn_initialize)
                .setOnClickListener(v -> initialize());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        destroyBanner();
        destroyInterstitial();
    }

    /**
     * Initialize MoPub SDK with BidMachineAdapterConfiguration
     */
    private void initialize() {
        //Initialize BidMachine SDK first
        BidMachine.setTestMode(true);
        BidMachine.setLoggingEnabled(true);
        BidMachine.initialize(this, "1");

        //Check initialized MoPub or not
        if (!MoPub.isSdkInitialized()) {
            Log.d(TAG, "MoPub initialize");

            //Prepare SdkConfiguration for initialize MoPub with BidMachineAdapterConfiguration
            SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(AD_UNIT_ID)
                    .withLogLevel(MoPubLog.LogLevel.DEBUG)
                    .withAdditionalNetwork(BidMachineAdapterConfiguration.class.getName())
                    .build();

            //Initialize MoPub SDK
            MoPub.initializeSdk(this, sdkConfiguration, new InitializationListener());
        } else {
            enableButton();
        }
    }

    /**
     * Enable buttons for user interaction
     */
    private void enableButton() {
        btnLoadBanner.setEnabled(true);
        btnShowBanner.setEnabled(true);
        btnLoadInterstitial.setEnabled(true);
        btnShowInterstitial.setEnabled(true);
        btnLoadRewardedVideo.setEnabled(true);
        btnShowRewardedVideo.setEnabled(true);
    }

    /**
     * Method for load banner from MoPub
     */
    private void loadBanner() {
        //Destroy previous MoPubView
        destroyBanner();

        Log.d(TAG, "MoPubView loadBanner");

        //Create new MoPubView instance and load
        moPubView = new MoPubView(this);
        moPubView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        moPubView.setAutorefreshEnabled(false);
        moPubView.setAdUnitId(BANNER_KEY);
        moPubView.setBannerAdListener(new BannerViewListener());
        moPubView.setVisibility(View.GONE);

        bannerContainer.addView(moPubView);

        BannerRequest bannerRequest = new BannerRequest.Builder()
                .setSize(BannerSize.Size_320x50)
                .setListener(new AdRequest.AdRequestListener<BannerRequest>() {
                    @Override
                    public void onRequestSuccess(@NonNull BannerRequest bannerRequest,
                                                 @NonNull AuctionResult auctionResult) {
                        // Fetch BidMachine Ads
                        Map<String, String> fetchParams = BidMachineFetcher.fetch(bannerRequest);
                        if (fetchParams != null) {
                            //Prepare MoPub keywords
                            String keywords = BidMachineUtils.toMopubKeywords(fetchParams);

                            //Request callbacks run in background thread, but you should call MoPub load methods on UI thread
                            runOnUiThread(() -> {
                                // Set MoPub Banner keywords
                                moPubView.setKeywords(keywords);

                                //Prepare localExtras for set to MoPubView with additional fetching parameters
                                Map<String, Object> localExtras = new HashMap<>(fetchParams);

                                //Set MoPub local extras
                                moPubView.setLocalExtras(localExtras);

                                //Load MoPub Ads
                                moPubView.loadAd();
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(
                                    BidMachineMoPubFetchActivity.this,
                                    "BannerFetchFailed",
                                    Toast.LENGTH_SHORT).show());
                        }
                    }

                    @Override
                    public void onRequestFailed(@NonNull BannerRequest bannerRequest,
                                                @NonNull BMError bmError) {
                        runOnUiThread(() -> Toast.makeText(
                                BidMachineMoPubFetchActivity.this,
                                "BannerFetchFailed",
                                Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onRequestExpired(@NonNull BannerRequest bannerRequest) {
                        //ignore
                    }
                })
                .build();

        //Request BidMachine Ads without load it
        bannerRequest.request(this);
    }

    /**
     * Method for show banner from MoPub
     */
    private void showBanner() {
        if (moPubView != null) {
            Log.d(TAG, "MoPubView showBanner");

            //Change MoPubView visibility
            moPubView.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "MoPubView null, load banner first");
        }
    }

    /**
     * Method for destroy MoPubView
     */
    private void destroyBanner() {
        if (moPubView != null) {
            Log.d(TAG, "MoPubView destroyBanner");

            bannerContainer.removeAllViews();
            moPubView.setBannerAdListener(null);
            moPubView.destroy();
        }
    }

    /**
     * Method for load interstitial from MoPub
     */
    private void loadInterstitial() {
        //Destroy previous MoPubInterstitial
        destroyInterstitial();

        Log.d(TAG, "MoPubInterstitial loadInterstitial");

        InterstitialRequest interstitialRequest = new InterstitialRequest.Builder()
                .setListener(new AdRequest.AdRequestListener<InterstitialRequest>() {
                    @Override
                    public void onRequestSuccess(@NonNull InterstitialRequest interstitialRequest,
                                                 @NonNull AuctionResult auctionResult) {
                        // Fetch BidMachine Ads
                        Map<String, String> fetchParams =
                                BidMachineFetcher.fetch(interstitialRequest);
                        if (fetchParams != null) {
                            //Prepare MoPub keywords
                            String keywords = BidMachineUtils.toMopubKeywords(fetchParams);

                            //Request callbacks run in background thread, but you should call MoPub load methods on UI thread
                            runOnUiThread(() -> {
                                //Create new MoPub Interstitial instance
                                moPubInterstitial = new MoPubInterstitial(
                                        BidMachineMoPubFetchActivity.this,
                                        INTERSTITIAL_KEY);
                                //Set MoPub interstitial listener if required
                                moPubInterstitial.setInterstitialAdListener(new InterstitialListener());

                                // Set MoPub Interstitial keywords
                                moPubInterstitial.setKeywords(keywords);

                                //Prepare localExtras for set to MoPubInterstitial with additional fetching parameters
                                Map<String, Object> localExtras = new HashMap<>(fetchParams);
                                localExtras.put("ad_content_type", "All");

                                //Set MoPub local extras
                                moPubInterstitial.setLocalExtras(localExtras);

                                //Load MoPub Ads
                                moPubInterstitial.load();
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(
                                    BidMachineMoPubFetchActivity.this,
                                    "InterstitialFetchFailed",
                                    Toast.LENGTH_SHORT).show());
                        }
                    }

                    @Override
                    public void onRequestFailed(@NonNull InterstitialRequest interstitialRequest,
                                                @NonNull BMError bmError) {
                        runOnUiThread(() -> Toast.makeText(
                                BidMachineMoPubFetchActivity.this,
                                "InterstitialFetchFailed",
                                Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onRequestExpired(@NonNull InterstitialRequest interstitialRequest) {
                        //ignore
                    }
                })
                .build();

        //Request BidMachine Ads without load it
        interstitialRequest.request(this);
    }

    /**
     * Method for show interstitial from MoPub
     */
    private void showInterstitial() {
        if (moPubInterstitial != null && moPubInterstitial.isReady()) {
            Log.d(TAG, "MoPubInterstitial showInterstitial");

            moPubInterstitial.show();
        } else {
            Log.d(TAG, "MoPubInterstitial null, load interstitial first");
        }
    }

    /**
     * Method for destroy MoPubInterstitial
     */
    private void destroyInterstitial() {
        if (moPubInterstitial != null) {
            Log.d(TAG, "MoPubInterstitial destroyInterstitial");

            moPubInterstitial.setInterstitialAdListener(null);
            moPubInterstitial.destroy();
        }
    }

    /**
     * Method for load rewarded video from MoPub
     */
    private void loadRewardedVideo() {
        Log.d(TAG, "MoPubRewardedVideos loadRewardedVideo");

        RewardedRequest request = new RewardedRequest.Builder()
                .setListener(new AdRequest.AdRequestListener<RewardedRequest>() {
                    @Override
                    public void onRequestSuccess(@NonNull RewardedRequest rewardedRequest,
                                                 @NonNull AuctionResult auctionResult) {
                        //Fetch BidMachine Ads
                        Map<String, String> fetchParams = BidMachineFetcher.fetch(rewardedRequest);
                        if (fetchParams != null) {
                            //Prepare MoPub keywords
                            String keywords = BidMachineUtils.toMopubKeywords(fetchParams);

                            //Request callbacks run in background thread, but you should call MoPub load methods on UI thread
                            runOnUiThread(() -> {
                                //Set MoPub Rewarded listener if required
                                MoPubRewardedVideos.setRewardedVideoListener(new RewardedVideoListener());

                                //Load MoPub Rewarded video
                                MoPubRewardedVideos.loadRewardedVideo(
                                        REWARDED_KEY,
                                        //Set MoPub Rewarded keywords
                                        new MoPubRewardedVideoManager.RequestParameters(keywords),
                                        //Create BidMachine MediationSettings with fetched request id
                                        new BidMachineMediationSettings(fetchParams));
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(
                                    BidMachineMoPubFetchActivity.this,
                                    "RewardedFetchFailed",
                                    Toast.LENGTH_SHORT).show());
                        }
                    }

                    @Override
                    public void onRequestFailed(@NonNull RewardedRequest rewardedRequest,
                                                @NonNull BMError bmError) {
                        runOnUiThread(() -> Toast.makeText(
                                BidMachineMoPubFetchActivity.this,
                                "RewardedFetchFailed",
                                Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onRequestExpired(@NonNull RewardedRequest rewardedRequest) {
                        //ignore
                    }
                })
                .build();

        //Request BidMachine Ads without load it
        request.request(this);
    }

    /**
     * Method for show rewarded video from MoPub
     */
    private void showRewardedVideo() {
        if (MoPubRewardedVideos.hasRewardedVideo(REWARDED_KEY)) {
            Log.d(TAG, "MoPubRewardedVideos showRewardedVideo");

            MoPubRewardedVideos.showRewardedVideo(REWARDED_KEY);
        } else {
            Log.d(TAG, "RewardedVideo not loaded");
        }
    }

    /**
     * Class for definition behavior after initialize finished
     */
    private class InitializationListener implements SdkInitializationListener {

        @Override
        public void onInitializationFinished() {
            Log.d(TAG, "MoPub onInitializationFinished");

            enableButton();
        }

    }

    /**
     * Class for definition behavior MoPubView
     */
    private class BannerViewListener implements MoPubView.BannerAdListener {

        @Override
        public void onBannerLoaded(MoPubView banner) {
            Log.d(TAG, "MoPubView onBannerLoaded");
            Toast.makeText(
                    BidMachineMoPubFetchActivity.this,
                    "BannerLoaded",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
            Log.d(TAG,
                  "MoPubView onBannerFailed with errorCode - " + errorCode.getIntCode() + " (" + errorCode
                          .toString() + ")");
            Toast.makeText(
                    BidMachineMoPubFetchActivity.this,
                    "BannerFailedToLoad",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBannerClicked(MoPubView banner) {
            Log.d(TAG, "MoPubView onBannerClicked");
        }

        @Override
        public void onBannerExpanded(MoPubView banner) {
            Log.d(TAG, "MoPubView onBannerExpanded");
        }

        @Override
        public void onBannerCollapsed(MoPubView banner) {
            Log.d(TAG, "MoPubView onBannerCollapsed");
        }

    }

    /**
     * Class for definition behavior MoPubInterstitial
     */
    private class InterstitialListener implements MoPubInterstitial.InterstitialAdListener {

        @Override
        public void onInterstitialLoaded(MoPubInterstitial interstitial) {
            Log.d(TAG, "MoPubInterstitial onInterstitialLoaded");
            Toast.makeText(
                    BidMachineMoPubFetchActivity.this,
                    "InterstitialLoaded",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
            Log.d(TAG,
                  "MoPubInterstitial onInterstitialFailed with errorCode - " + errorCode.getIntCode() + " (" + errorCode
                          .toString() + ")");
            Toast.makeText(
                    BidMachineMoPubFetchActivity.this,
                    "InterstitialFailedToLoad",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onInterstitialShown(MoPubInterstitial interstitial) {
            Log.d(TAG, "MoPubInterstitial onInterstitialShown");
        }

        @Override
        public void onInterstitialClicked(MoPubInterstitial interstitial) {
            Log.d(TAG, "MoPubInterstitial onInterstitialClicked");
        }

        @Override
        public void onInterstitialDismissed(MoPubInterstitial interstitial) {
            Log.d(TAG, "MoPubInterstitial onInterstitialDismissed");
        }

    }

    /**
     * Class for definition behavior MoPubRewardedVideos
     */
    private class RewardedVideoListener implements MoPubRewardedVideoListener {

        @Override
        public void onRewardedVideoLoadSuccess(@NonNull String adUnitId) {
            Log.d(TAG, "MoPubRewardedVideos onRewardedVideoLoadSuccess");
            Toast.makeText(
                    BidMachineMoPubFetchActivity.this,
                    "RewardedVideoLoaded",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRewardedVideoLoadFailure(@NonNull String adUnitId,
                                               @NonNull MoPubErrorCode errorCode) {
            Log.d(TAG,
                  "MoPubRewardedVideos onRewardedVideoLoadFailure with errorCode - " + errorCode.getIntCode() + " (" + errorCode
                          .toString() + ")");
            Toast.makeText(
                    BidMachineMoPubFetchActivity.this,
                    "RewardedVideoFailedToLoad",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRewardedVideoStarted(@NonNull String adUnitId) {
            Log.d(TAG, "MoPubRewardedVideos onRewardedVideoStarted");
        }

        @Override
        public void onRewardedVideoPlaybackError(@NonNull String adUnitId,
                                                 @NonNull MoPubErrorCode errorCode) {
            Log.d(TAG,
                  "MoPubRewardedVideos onRewardedVideoPlaybackError with errorCode - " + errorCode.getIntCode() + " (" + errorCode
                          .toString() + ")");
        }

        @Override
        public void onRewardedVideoClicked(@NonNull String adUnitId) {
            Log.d(TAG, "MoPubRewardedVideos onRewardedVideoClicked");
        }

        @Override
        public void onRewardedVideoClosed(@NonNull String adUnitId) {
            Log.d(TAG, "MoPubRewardedVideos onRewardedVideoClosed");
        }

        @Override
        public void onRewardedVideoCompleted(@NonNull Set<String> adUnitIds,
                                             @NonNull MoPubReward reward) {
            Log.d(TAG, "MoPubRewardedVideos onRewardedVideoCompleted");
        }

    }
}
