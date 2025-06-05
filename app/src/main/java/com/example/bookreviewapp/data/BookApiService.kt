package com.example.bookreviewapp.data

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
