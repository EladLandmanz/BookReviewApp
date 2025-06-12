package com.example.bookreviewapp.data

import retrofit2.http.GET
import retrofit2.http.Path

interface OpenLibraryApi{
    @GET("works/{id}.json")
    suspend fun getBookDetails(@Path("id") id: String): WorkDetailsResponse
}