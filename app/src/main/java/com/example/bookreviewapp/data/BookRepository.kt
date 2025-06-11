package com.example.bookreviewapp.data

import com.example.bookreviewapp.data.BookSearchResponse
import javax.inject.Inject

class BookRepository @Inject constructor(
    private val apiService: BookApiService
) {
    // This function fetches books from the API using coroutines (suspend)
    suspend fun getMostPublishedBooks(): BookSearchResponse {
        return apiService.getMostPublishedBooks()
    }
}
