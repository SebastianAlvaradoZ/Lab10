package com.example.lab10.data

import com.google.gson.annotations.SerializedName

// Modelo de datos que representa una serie tal como la devuelve la API de Django.
// @SerializedName indica el nombre exacto del campo en el JSON, para que Gson
// pueda convertir automáticamente JSON <-> objeto Kotlin.
data class SerieModel(
    @SerializedName("id")
    var id: Int,
    @SerializedName("name")
    var name: String,
    @SerializedName("release_date")
    var release_date: String,
    @SerializedName("rating")
    var rating: Int,
    @SerializedName("category")
    var category: String
)
