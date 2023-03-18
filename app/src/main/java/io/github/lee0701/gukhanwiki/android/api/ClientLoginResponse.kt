package io.github.lee0701.gukhanwiki.android.api

import com.google.gson.annotations.SerializedName

data class ClientLoginResponse(
    @SerializedName("error") val error: Map<String, String>? = null,
    @SerializedName("clientlogin") val clientLogin: ClientLoginResult? = null,
)

data class ClientLoginResult(
    @SerializedName("status") val status: String,
    @SerializedName("username") val username: String,
)
