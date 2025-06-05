package com.example.bookreviewapp.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.bookreviewapp.entities.Book

@Dao
interface BookDao {
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun addBook(book: Book)

    //delete a book from the app
    @Delete
    fun deleteBook(vararg book: Book)

    //update an existing book
    @Update
    fun update(book: Book)

    //get the top 10 rated books
    @Query ("SELECT * FROM books ORDER BY rating DESC LIMIT 10")
    fun getTopRatedBooks() : LiveData<List<Book>>

    // get a book by his title
    @Query ("SELECT * FROM books WHERE title = :bookTitle")
    fun getBookByTitle(bookTitle: String) : Book
}