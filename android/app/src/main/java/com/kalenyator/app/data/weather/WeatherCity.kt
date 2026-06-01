package com.kalenyator.app.data.weather

import com.google.gson.annotations.SerializedName

data class WeatherCity(
    @SerializedName("name") val name: String,
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lon") val longitude: Double,
    @SerializedName("home") val isHome: Boolean = false
)
