package com.example.bookreviewapp.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.example.bookreviewapp.Utils.Resource
import com.example.bookreviewapp.Utils.performFetchingAndSaving
import com.example.bookreviewapp.dao.BookDao
import com.example.bookreviewapp.entities.Book
import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.internal.NopCollector.emit
import javax.inject.Inject

class BookRepository @Inject constructor(
    private val apiService: BookApiService,
    private val bookDao: BookDao
) {
    // This function fetches books from the API using coroutines (suspend)
    suspend fun getTrendingBooks() = apiService.getTrendingBooks()
    suspend fun searchBooks(query: String) = apiService.searchBooks(query)

    suspend fun getBooksBySubject(subject: String): SubjectResponse {
        return apiService.getBooksBySubject(subject)
    }


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

    fun deleteBook(book: Book) {
        bookDao.deleteBook(book)
    }

    suspend fun updateBook(book: Book) {
        Log.d("RoomUpdate", "Updating book: ${book.id} favorite=${book.isFavorite}")
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

    fun withCacheGetFavoriteBooks(): LiveData<Resource<List<Book>>>{
        return liveData(Dispatchers.IO) { // Run the whole block on IO dispatcher

            emit(Resource.loading()) // Immediately emit a loading state

            // Observe the LiveData from the DAO.

            val source = bookDao.getAllFavoriteBooks().map { entities ->

                val books = entities
                Resource.success(books) // Wrap the list in a Resource.success
            }
            emitSource(source) // Start emitting values from the transformed local DB LiveData
        }
    }


    fun withCacheGetBookDetails(bookId: String): LiveData<Resource<Book>>{
        return performFetchingAndSaving(
            localDbFetch = {
                // Fetch single book from local DB and map to domain model
                bookDao.getBookById(bookId)/*.map { entity ->
                    entity?.toBook() // Map nullable entity to nullable domain book
                }*/
            },
            remoteDbFetch = {
                // Fetch single book from remote API and wrap in Resource
                try {
                    val apiBook = apiService.getBookDetails(bookId) // Suspend call
                    Resource.success(apiBook) // apiBook is BookDto
                } catch (e: Exception) {
                    Resource.error("Failed to fetch book details: ${e.localizedMessage}")
                }
            },
            localDbSave = { apiBook ->
                // Save fetched API book (BookDto) to local DB (BookEntity)
                bookDao.addBook(mapWorkDetailsToBook(bookId, apiBook))
            }
        )
    }


    fun withCacheGetTrendingBooks(): LiveData<Resource<List<Book>>>{
      TODO("maybe implement if we would save the trending books locally")
    }

}
