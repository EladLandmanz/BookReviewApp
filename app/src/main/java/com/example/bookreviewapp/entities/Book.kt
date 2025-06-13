package com.example.bookreviewapp.entities
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book")
data class Book(
    @PrimaryKey val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "author")
    val author: String,

    @ColumnInfo(name = "summary")
    val summary: String?,

    @ColumnInfo(name = "rating")
    var rating: Float,

    @ColumnInfo(name = "image")
    val imageUrl: String?,

    @ColumnInfo(name = "isFavorite")
    var isFavorite: Boolean = false



)

object BookManager {
    val books:MutableList<Book> = mutableListOf()

    fun add (book: Book){
        books.add(book)
    }
}