package io.github.lee0701.gukhanwiki.android.api.action

import com.google.gson.annotations.SerializedName

data class RandomResponse(
    @SerializedName("error") val error: Map<String, String>? = null,
    @SerializedName("query") val result: RandomResult? = null,
)

data class RandomResult(
    @SerializedName("random") val random: List<RandomItem>? = null,
)

data class RandomItem(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("ns") val namespace: Int? = null,
    @SerializedName("title") val title: String? = null,
)
