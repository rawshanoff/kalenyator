package com.kalenyator.app.data.weather

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = 8,
        @Query("language") language: String = "ru"
    ): GeocodingResponse
}

data class GeocodingResponse(val results: List<GeocodingResult>?)

data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    @SerializedName("admin1") val admin1: String? = null
) {
    fun displayName(): String = buildString {
        append(name)
        admin1?.let { append(", $it") }
        country?.let { append(" ($it)") }
    }
}
