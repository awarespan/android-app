package com.awarespan.guardian.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class VolunteerSourceDto {
    @SerializedName("sourceId") public String sourceId;
    @SerializedName("deviceName") public String deviceName;
    @SerializedName("sourceType") public String sourceType;
    @SerializedName("status") public String status;
    @SerializedName("deviceModel") public String deviceModel;
    @SerializedName("lastSeen") public String lastSeen;
}
