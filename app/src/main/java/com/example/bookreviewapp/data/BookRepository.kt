package com.example.bookreviewapp.data

import javax.inject.Inject

class BookRepository @Inject constructor(
    private val apiService: BookApiService
) {
    // This function fetches books from the API using coroutines (suspend)
    suspend fun getTrendingBooks() = apiService.getTrendingBooks()
    suspend fun searchBooks(query: String) = apiService.searchBooks(query)

}
