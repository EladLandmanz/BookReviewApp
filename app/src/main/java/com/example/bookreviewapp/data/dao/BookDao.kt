package com.example.bookreviewapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.bookreviewapp.data.models.Book

@Dao
interface BookDao {
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBook(book: Book)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBooks(books: List<Book>)

    //delete a book from the app
    @Delete
    fun deleteBook(vararg book: Book)

    //update an existing book
    @Update
    suspend fun updateBook(book: Book)

    //get the top 10 rated books
    @Query ("SELECT * FROM book ORDER BY rating DESC LIMIT 10")
    fun getTopRatedBooks() : LiveData<List<Book>>

    // get a book by his title
    @Query ("SELECT * FROM book WHERE title = :bookTitle")
    fun getBookByTitle(bookTitle: String) : LiveData<Book>

    @Query ("SELECT * FROM book")
    fun getAllBooks(): LiveData<List<Book>>

    @Query ("SELECT * FROM book WHERE id = :bookId")
    fun getBookById(bookId: String) : LiveData<Book?>

    @Query("SELECT * FROM book WHERE isFavorite = 1")
    fun getAllFavoriteBooks(): LiveData<List<Book>>

    @Query ("SELECT * FROM book WHERE id = :bookId LIMIT 1")
    suspend fun getBookByIdSuspend(bookId: String) : Book?

    @Query("SELECT * FROM book WHERE review IS NOT NULL AND review != ''")
    fun getBooksWithReviews(): LiveData<List<Book>>

}