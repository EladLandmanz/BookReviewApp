package com.example.bookreviewapp.data

import androidx.room.Query
import retrofit2.http.GET

data class BookApiResult(
    val key: String,
    val title: String,
    val author_name: List<String>?,
    val edition_count: Int,
    val subject: List<String>?,
    val language: List<String>?,
    val first_publish_year: Int?
)

data class BookSearchResponse(
    val docs: List<BookApiResult>
)
interface BookApiService {
    @GET("search.json")
    suspend fun getMostPublishedBooks(
        @Query("sort") sort: String = "edition_count",
        @Query("limit") limit: Int = 10
    ): BookSearchResponse

}
