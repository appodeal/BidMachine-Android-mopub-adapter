package com.mopub.mobileads;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mopub.common.MediationSettings;

import java.util.Map;

import io.bidmachine.BidMachineFetcher;

public class BidMachineMediationSettings implements MediationSettings {

    @Nullable
    private String requestId;

    @SuppressWarnings("unused")
    public BidMachineMediationSettings() {
    }

    public BidMachineMediationSettings(@NonNull Map<String, String> params) {
        withRequestId(params);
    }

    @SuppressWarnings("WeakerAccess")
    public BidMachineMediationSettings withRequestId(@Nullable String requestId) {
        this.requestId = requestId;
        return this;
    }

    @SuppressWarnings("WeakerAccess")
    public BidMachineMediationSettings withRequestId(@NonNull Map<String, String> params) {
        return withRequestId(params.get(BidMachineFetcher.KEY_ID));
    }

    @Nullable
    @SuppressWarnings("WeakerAccess")
    public String getRequestId() {
        return requestId;
    }
}
