package com.example.bookreviewapp.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.example.bookreviewapp.utils.Resource
import com.example.bookreviewapp.utils.mapWorkToBook
import com.example.bookreviewapp.utils.performFetchingAndSaving
import com.example.bookreviewapp.utils.toBook
import kotlinx.coroutines.Dispatchers
import com.example.bookreviewapp.data.remote_db.BookApiService
import com.example.bookreviewapp.data.remote_db.SubjectResponse
import com.example.bookreviewapp.data.remote_db.WorkDetailsResponse
import com.example.bookreviewapp.data.dao.BookDao
import com.example.bookreviewapp.data.models.Book
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

    suspend fun getBookFromDbSync(bookId: String): LiveData<Book?> = bookDao.getBookById(bookId)

    fun getAllFavoriteBooks(): LiveData<List<Book>> = bookDao.getAllFavoriteBooks()

    fun getAllBooks(): LiveData<List<Book>> = bookDao.getAllBooks()

    fun getBookByTitle(title: String): LiveData<Book> = bookDao.getBookByTitle(title)

    fun getBookFromDb(bookId: String): LiveData<Book?> = bookDao.getBookById(bookId)

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

    fun getBooksWithReviews(): LiveData<List<Book>> {
        return bookDao.getBooksWithReviews()
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
                Resource.success(entities)
            }
            emitSource(source)
        }
    }


    fun withCacheGetBookDetails(bookId: String): LiveData<Resource<Book>>{
        return performFetchingAndSaving(
            localDbFetch = {
                // Fetch single book from local DB, if the book does not exist locally
                bookDao.getBookById(bookId).map { book ->
                    book ?: Book(
                        id = "Loading",
                        title = "",
                        summary = "",
                        imageUrl = "",
                        rating = 0f,
                        author = ""
                    )

                }
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
                val existingBook = bookDao.getBookByIdSuspend(bookId)
                val mergedBookEntity = apiBook.mapWorkToBook(
                    bookId,
                    existingIsFavorite = existingBook?.isFavorite,
                    existingRating = existingBook?.rating
                )
                bookDao.addBook(mergedBookEntity)

            }
        )
    }


    fun withCacheGetTrendingBooks(): LiveData<Resource<List<Book>>>{
      return performFetchingAndSaving(
          localDbFetch = {
              bookDao.getAllBooks()
          },
          remoteDbFetch = {
            try {
                val apiBooks = apiService.getTrendingBooks()
                Resource.success(apiBooks.docs)
            }catch (e: Exception) {
                Resource.error("Failed to fetch trending books ${e.localizedMessage}")
            }

          },
          localDbSave = { apiBooks ->
              val mergedBookEntities = apiBooks.map { apiBook ->
                      val existingBookEntity =
                          bookDao.getBookByIdSuspend(apiBook.key ?: "")
                      apiBook.toBook(
                          existingIsFavorite = existingBookEntity?.isFavorite,
                          existingRating = existingBookEntity?.rating
                      )
              }
              //get the list of searchBooks, map them to book entities and store in the DB.
              bookDao.addBooks(mergedBookEntities)

          }
      )
    }
    suspend fun updateBookFavoriteStatus(bookId: String, isFavorite: Boolean): Boolean {
        return try {

            val existingBookEntity = bookDao.getBookByIdSuspend(bookId)

            Log.d("FavoriteDebug", "Book ID: ${existingBookEntity?.id}, New Favorite: ${existingBookEntity?.isFavorite}")

            if (existingBookEntity != null) {

                val updatedBookEntity = existingBookEntity.copy(isFavorite = isFavorite)


                bookDao.updateBook(updatedBookEntity)
                Log.d("FavoriteDebug", "Book ID: ${updatedBookEntity.id}, New Favorite: ${updatedBookEntity.isFavorite}")
                true
            } else {
                Log.d("FavoriteDebug", "book is null ")
                false
            }
        } catch (e: Exception) {
            Log.d("updateFavoriteStatus", "exception in updating $bookId")
            e.printStackTrace()
            false
        }
    }

}
