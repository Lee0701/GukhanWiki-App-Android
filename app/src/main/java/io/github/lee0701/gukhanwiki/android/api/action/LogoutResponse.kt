package io.github.lee0701.gukhanwiki.android.api.action

import com.google.gson.annotations.SerializedName

data class LogoutResponse(
    @SerializedName("error") val error: Map<String, String>? = null,
)