package com.example.bookreviewapp.data

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.bookreviewapp.dao.BookDao
import com.example.bookreviewapp.entities.Book
import javax.inject.Inject

class BookRepository @Inject constructor(
    private val apiService: BookApiService,
    private val bookDao: BookDao
) {
    // This function fetches books from the API using coroutines (suspend)
    suspend fun getTrendingBooks() = apiService.getTrendingBooks()
    suspend fun searchBooks(query: String) = apiService.searchBooks(query)

    suspend fun fetchBookFromApi(bookId: String): WorkDetailsResponse {
        Log.d("load", "fetch from API ${bookId}")
        return apiService.getBookDetails(bookId)
    }

    suspend fun getBookByIdSuspend(bookId: String): Book? =
        bookDao.getBookByIdSuspend(bookId)

    fun getBookFromDbSync(bookId: String): LiveData<Book> = bookDao.getBookById(bookId)

    fun getAllFavoriteBooks(): LiveData<List<Book>> = bookDao.getAllFavoriteBooks()

    fun getAllBooks(): LiveData<List<Book>> = bookDao.getAllBooks()

    fun getBookByTitle(title: String): LiveData<Book> = bookDao.getBookByTitle(title)

    fun getBookFromDb(bookId: String): LiveData<Book> = bookDao.getBookById(bookId)

    fun getRecommendedBooks(): LiveData<List<Book>> = bookDao.getTopRatedBooks()

    suspend fun addBook(book: Book) {
        bookDao.addBook(book)
    }

    suspend fun deleteBook(book: Book) {
        bookDao.deleteBook(book)
    }

    suspend fun updateBook(book: Book) {
        bookDao.updateBook(book)
    }

    fun mapWorkDetailsToBook(id: String, response: WorkDetailsResponse): Book {
        val imageUrl = response.covers?.firstOrNull()?.let {
            "https://covers.openlibrary.org/b/id/$it-L.jpg"
        }

        val title = response.title.toString()
        val summary = when (response.description) {
            is String -> response.description as String
            is Map<*, *> -> (response.description as Map<*, *>)["value"] as? String
            else -> null
        }
        val authorId = response.authors?.firstOrNull()?.author?.key.toString()
        Log.d("workMap", "title ${title}, id ${id}")
        return Book(
            id = id,
            title = title,
            summary = summary,
            imageUrl = imageUrl,
            rating = 0f,
            author = authorId
        )
    }

}
