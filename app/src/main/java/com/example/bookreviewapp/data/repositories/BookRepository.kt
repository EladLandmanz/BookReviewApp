package com.example.bookreviewapp.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.example.bookreviewapp.data.BookCategory
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
import com.example.bookreviewapp.utils.Loading
import com.example.bookreviewapp.utils.Success
import com.example.bookreviewapp.utils.Error
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

    fun withLoadingSearchBooks(query: String):  LiveData<Resource<List<Book>>>{
        return liveData(Dispatchers.IO) { // Run the whole block on IO dispatcher

            emit(Resource.loading()) // Immediately emit a loading state

            try {
                val response = apiService.searchBooks(query)
                val apiBooks = response.docs
                Log.d("SearchRepo", "query: $query")
                Log.d("SearchRepo", "response ${response.docs.size} books")

                val mergedBookEntities = apiBooks.map { apiBook ->
                    val existingBookEntity = bookDao.getBookByIdSuspend(apiBook.key ?: "") // Suspend DAO call
                    apiBook.toBook(
                        existingIsFavorite = existingBookEntity?.isFavorite,
                        existingRating = existingBookEntity?.rating,
                        existingTrending = existingBookEntity?.isTrending
                    )
                }
                bookDao.addBooks(mergedBookEntities)
                Log.d("SearchRepo", "got ${mergedBookEntities.size} books")
                val domainBooks = apiBooks.map { it.toBook() }

                emit(Resource.success(domainBooks))

            }catch (e: Exception){
                emit(Resource.error("Failed to search for books: ${e.localizedMessage}"))
            }

        }
    }

    fun withCacheGetFavoriteBooks(): LiveData<Resource<List<Book>>>{
        return liveData(Dispatchers.IO) {

            emit(Resource.loading()) // Immediately emit a loading state

            // get the LiveData from the DAO. map to Resource.success.

            val source = bookDao.getAllFavoriteBooks().map { entities ->
                Resource.success(entities)
            }
            emitSource(source)
        }
    }


    fun withCacheGetBookDetails(bookId: String, forceNewBook: Boolean): LiveData<Resource<Book>>{
        return performFetchingAndSaving(
            localDbFetch = {
                // Fetch single book from local DB, if the book does not exist locally
                bookDao.getBookById(bookId).map { book ->

                    Log.d("cacheRepo", "LiveData from DAO emitted book with title: ${book?.title}")
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

                //if the book is already translated and the language is hebrew, do not overwrite with the api
                if(existingBook?.isTranslated == true && !forceNewBook){
                    Log.d("cacheRepo", "book is translated, do not overwrite")
                }else {

                    val mergedBookEntity = apiBook.mapWorkToBook(
                        bookId,
                        existingIsFavorite = existingBook?.isFavorite,
                        existingRating = existingBook?.rating,
                        existingTrending = existingBook?.isTrending
                    )
                    mergedBookEntity.isTranslated = false
                    Log.d("cacheRepo", "add book: $mergedBookEntity")
                    bookDao.addBook(mergedBookEntity)
                }
            }
        )
    }


    fun withCacheGetTrendingBooks(): LiveData<Resource<List<Book>>>{
      return performFetchingAndSaving(
          localDbFetch = {
              bookDao.getTrendingBooksLocalOnly()
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
                          existingRating = existingBookEntity?.rating,
                          existingTrending = true
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

    fun getBooksBySubjectCached(subject: String): LiveData<Resource<List<Book>>>  {
        return performFetchingAndSaving(
            localDbFetch = {
                bookDao.getBooksBySubjectLocalOnly(subject)
            },
            remoteDbFetch = {
                try {
                    val response = apiService.getBooksBySubject(subject) // Fetch 5 books for the subject
                    Resource.success(response.works) // Assuming it returns docs
                } catch (e: Exception) {
                    Resource.error("Failed to fetch books for subject $subject: ${e.localizedMessage}")
                }
            },
            localDbSave = { apiBooks ->
                val mergedBookEntities = apiBooks.map { apiBook ->
                    val existingBookEntity =
                        bookDao.getBookByIdSuspend(apiBook.key ?: "")
                    apiBook.toBook(
                        existingIsFavorite = existingBookEntity?.isFavorite,
                        existingRating = existingBookEntity?.rating,
                        existingSubject = subject
                    )
                }
                //get the list of searchBooks, map them to book entities and store in the DB.
                bookDao.addBooks(mergedBookEntities)

            }
        )




//        emit(Resource.loading()) // Emit loading state immediately
//
//        try {
//            val response = apiService.getBooksBySubject(subject) // Make network call
//            val apiBooks = response.works // Extract the list of api Books
//

//            bookDao.addBooks(mergedBookEntities) // Insert/update individual books in the main table
//            emit(Resource.success(mergedBookEntities)) // Emit success with the fetched books
//
//        } catch (e: Exception) {
//            emit(Resource.error("Failed to load ${subject} books: ${e.localizedMessage}")) // Emit error
//        }
    }



    fun getBooksGroupedBySubjects(subjects: List<String>): LiveData<Resource<List<BookCategory>>> {
        //helps managing several livedata
        val resultLiveData = MediatorLiveData<Resource<List<BookCategory>>>()
        //hold each category of books in a hash map
        val sources = mutableMapOf<String, LiveData<Resource<List<Book>>>>()
        val latestResults = mutableMapOf<String, List<Book>?>()


        Log.d("SubjectRepo", "Starting aggregation for subjects: $subjects")


        // Set an initial loading state
        resultLiveData.value = Resource.loading(emptyList())

        // Create and add sources for each subject
        subjects.forEach { subject ->
            val subjectLiveData = getBooksBySubjectCached(subject) // Get LiveData<Resource<List<Book>>> for each subject
            sources[subject] = subjectLiveData

            resultLiveData.addSource(subjectLiveData) { resource ->
                // This block executes whenever any of the individual subject LiveData emits.
                Log.d("SubjectRepo", "Source emitted for subject: $subject. Resource status: ${resource.status.javaClass.simpleName}")
                //store the books of this subject
                latestResults[subject] = resource.status.data
                // Aggregate current states
                val currentCategories = mutableListOf<BookCategory>()
                var overallLoading = false
                var overallError: String? = null
                var hasAnyError = false // Track if any source has an error

                //for each subject that emits, go through all subjects again and check their status
                subjects.forEach { s ->
                    val sourceResource = sources[s]?.value // Get the current value from the source LiveData

                    Log.d("SubjectRepo", "  Aggregating source for '$s'. Current status: ${sourceResource?.status?.javaClass?.simpleName ?: "NULL_RESOURCE"}")
                    when (sourceResource?.status) { // Null check for resource.value
                        is Loading -> {
                            overallLoading = true
                        }
                        is Success -> {
                            //when data is fetched add this book category and the books
                            sourceResource.status.data?.let { books ->
                                currentCategories.add(BookCategory(s, books))
                            } ?: run {
                                // A success with null
                                currentCategories.add(BookCategory(s, emptyList()))
                            }
                        }
                        is Error -> {
                            hasAnyError = true
                           // overallError = resource // Take the last error message
                            sourceResource.status.data?.let { books ->
                                currentCategories.add(BookCategory(s, books)) // Add stale data on error
                            } ?: run {
                                // Add an empty category if error and no stale data
                                currentCategories.add(BookCategory(s, emptyList()))
                            }
                        }
                        null -> { // If source hasn't emitted yet
                            overallLoading = true
                            currentCategories.add(BookCategory(s, emptyList()))
                        }
                    }
                }

                // Post the combined result
                if (overallLoading) {
                    Log.d("SubjectRepo", "Overall: Loading. Categories collected: ${currentCategories.size}")
                    resultLiveData.value = Resource.loading(currentCategories.toList())
                } else if (hasAnyError) {
                    Log.e("SubjectRepo", "Overall: Error. Categories collected: ${currentCategories.size}. Error: $overallError")
                    resultLiveData.value = Resource.error(overallError ?: "Unknown error", currentCategories.toList())
                } else {
                    //successful fetch
                    Log.d("SubjectRepo", "Overall: Success. Categories collected: ${currentCategories.size}")
                    resultLiveData.value = Resource.success(currentCategories.toList())
                }
            }
        }
        return resultLiveData
    }







}
