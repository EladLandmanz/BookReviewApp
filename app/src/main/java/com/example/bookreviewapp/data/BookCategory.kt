package com.example.bookreviewapp.data

import com.example.bookreviewapp.Book

data class BookCategory(
    val subject: String,
    val books: List<Book>
)
