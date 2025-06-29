package com.example.bookreviewapp.data.remote_db

import com.example.bookreviewapp.data.models.Book

data class BookCategory(
    val subject: String,
    val books: List<Book>
)
