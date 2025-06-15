package com.example.bookreviewapp

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val rating: Float,
    val summary: String,
    val imageUrl: String
)