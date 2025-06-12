package com.example.bookreviewapp.data


import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit interface
interface BookApiService {
    @GET("search.json?q=the")
    suspend fun getTrendingBooks(): SearchApiResponse

    @GET("search.json")
    suspend fun searchBooks(@Query("q") query: String):SearchApiResponse
}


data class SearchApiResponse(
    val docs: List<SearchBook>
)

data class SearchBook(
    val key: String?,
    val title: String?,
    val author_name: List<String>?,
    val cover_i: Int?,
    val cover_edition_key: String?,
    val edition_count: Int?
)


