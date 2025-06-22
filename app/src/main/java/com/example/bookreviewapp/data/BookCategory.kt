package com.example.bookreviewapp.data

import com.example.bookreviewapp.data.models.Book

data class BookCategory(
    val subject: String,
    val books: List<Book>
)
