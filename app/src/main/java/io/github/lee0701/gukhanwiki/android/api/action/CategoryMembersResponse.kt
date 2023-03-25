package io.github.lee0701.gukhanwiki.android.api.action

import com.google.gson.annotations.SerializedName

data class CategoryMembersResponse(
    @SerializedName("error") val error: Map<String, String>? = null,
    @SerializedName("continue") val queryContinue: CategoryMembersContinue? = null,
    @SerializedName("query") val result: CategoryMembersResult? = null,
)

data class CategoryMembersContinue(
    @SerializedName("cmcontinue") val cmContinue: String? = null,
)

data class CategoryMembersResult(
    @SerializedName("categorymembers") val categoryMembers: List<CategoryMembersItem>? = null,
)

data class CategoryMembersItem(
    @SerializedName("pageid") val pageId: Int? = null,
    @SerializedName("ns") val namespace: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("sortkey") val sortKey: String? = null,
)
