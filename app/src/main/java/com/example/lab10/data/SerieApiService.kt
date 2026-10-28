package com.example.lab10.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Interfaz que describe los endpoints de la API REST de series.
// Retrofit genera automáticamente la implementación de cada función
// a partir de las anotaciones (verbo HTTP + ruta relativa a la URL base).
interface SerieApiService {

    // GET /serie -> obtiene el listado completo de series
    @GET("serie")
    suspend fun selectSeries(): ArrayList<SerieModel>

    // GET /serie/{id} -> obtiene una sola serie por su id
    @GET("serie/{id}")
    suspend fun selectSerie(@Path("id") id: String): Response<SerieModel>

    // POST /serie -> crea una nueva serie (el objeto se envía como JSON en el cuerpo)
    @Headers("Content-Type: application/json")
    @POST("serie")
    suspend fun insertSerie(@Body serie: SerieModel): Response<SerieModel>

    // PUT /serie/{id} -> actualiza una serie existente
    @PUT("serie/{id}")
    suspend fun updateSerie(@Path("id") id: String, @Body serie: SerieModel): Response<SerieModel>

    // DELETE /serie/{id} -> elimina una serie
    @DELETE("serie/{id}")
    suspend fun deleteSerie(@Path("id") id: String): Response<SerieModel>
}
