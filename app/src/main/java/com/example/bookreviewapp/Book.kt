package com.example.bookreviewapp

data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val rating: Float,
    val summary: String,
    val imageUrl: String
)

object ItemsManger {

    val resultBooks : MutableList<Book> = mutableListOf()

    fun add(book: Book) {
        resultBooks.add(book)
    }

    fun remove(index:Int){
        resultBooks.removeAt(index)
    }
}