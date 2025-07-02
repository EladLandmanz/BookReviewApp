package com.example.bookreviewapp.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.bookreviewapp.data.remote_db.BookCategory
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
import com.example.bookreviewapp.data.workers.ListTranslationWorker
import com.example.bookreviewapp.utils.Loading
import com.example.bookreviewapp.utils.Success
import com.example.bookreviewapp.utils.Error
import com.example.bookreviewapp.utils.LangProvider
import javax.inject.Inject

class BookRepository @Inject constructor(
    private val apiService: BookApiService,
    private val bookDao: BookDao,
    private val langProvider: LangProvider,
    private val workManager: WorkManager
) {
    suspend fun searchBooks(query: String) = apiService.searchBooks(query)


    suspend fun updateBook(book: Book) {
        bookDao.updateBook(book)
    }

    fun getBooksWithReviews(): LiveData<List<Book>> {
        return bookDao.getBooksWithReviews()
    }

    //get the search results while emitting the Resource for the ui to display loading or error
    fun withLoadingSearchBooks(query: String):  LiveData<Resource<List<Book>>>{
        return liveData(Dispatchers.IO) { // Run the whole block on IO dispatcher

            emit(Resource.loading()) // Immediately emit a loading state

            try {
                //get the books from the api
                val response = apiService.searchBooks(query)
                val apiBooks = response.docs
                Log.d("SearchRepo", "query: $query")
                Log.d("SearchRepo", "response ${response.docs.size} books")

                //save the books from the api while keeping the saved user data of each book to avoid overwriting with the api
                val mergedBookEntities = apiBooks.map { apiBook ->
                    val existingBookEntity = bookDao.getBookByIdSuspend(apiBook.key ?: "")
                    apiBook.toBook(
                        existingIsFavorite = existingBookEntity?.isFavorite,
                        existingRating = existingBookEntity?.rating,
                        existingTrending = existingBookEntity?.isTrending,
                        existingReview = existingBookEntity?.review
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
    //get the favorite books wrapped in a resource to emit it status and display it in the fragment
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
    //get the book details using performFetchingAndSaving
    //first emit loading then emit the locally saved result if one exist
    //then fetch from the api
    //then save to room with regard to user data
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
                    //save the books from the api while keeping the saved user data of each book avoid overwriting with the api
                    val mergedBookEntity = apiBook.mapWorkToBook(
                        bookId,
                        existingIsFavorite = existingBook?.isFavorite,
                        existingRating = existingBook?.rating,
                        existingTrending = existingBook?.isTrending,
                        existingReview = existingBook?.review
                    )
                    mergedBookEntity.isTranslated = false
                    Log.d("cacheRepo", "add book: $mergedBookEntity")
                    bookDao.addBook(mergedBookEntity)
                }
            }
        )
    }

    //get the trending books using performFetchingAndSaving
    //first emit loading then emit the locally saved result if they exist
    //then fetch from the api
    //then save to room with regard to user  to avoid overwriting
    fun withCacheGetTrendingBooks(forceNewBook: Boolean): LiveData<Resource<List<Book>>>{
      return performFetchingAndSaving(
          localDbFetch = {
              Log.d("cacheRepo", "Trending books local fetch")
              bookDao.getTrendingBooksLocalOnly()

          },
          remoteDbFetch = {
              Log.d("cacheRepo", "Trending books remote fetch")
            try {
                val apiBooks = apiService.getTrendingBooks()
                Resource.success(apiBooks.docs)
            }catch (e: Exception) {
                Resource.error("Failed to fetch trending books ${e.localizedMessage}")
            }

          },
          localDbSave = { apiBooks ->
              Log.d("cacheRepo", "Trending books local save")
              val mergedBookEntities = apiBooks.map { apiBook ->
                  val existingBookEntity = bookDao.getBookByIdSuspend(apiBook.key ?: "")
                  Log.d("cacheRepo", "apibook: ${apiBook.title}")
                  Log.d("cacheRepo", "book: ${existingBookEntity?.title} translted? ${existingBookEntity?.isTranslated}")
                  //if the book is already translated and the language is hebrew, dont update it
                  if(existingBookEntity?.isTranslated == true && !forceNewBook){
                      Log.d("cacheRepo", " not overwritting book: ${existingBookEntity.title}")
                      existingBookEntity
                  }else {
                      Log.d("cacheRepo", "book: ${existingBookEntity?.title} ")
                      //save the books from the api while keeping the saved user data of each book avoid overwriting with the api
                      apiBook.toBook(
                          existingIsFavorite = existingBookEntity?.isFavorite,
                          existingRating = existingBookEntity?.rating,
                          existingReview = existingBookEntity?.review,
                          existingTrending = true
                      )
                  }
              }
              //get the list of searchBooks, map them to book entities and store in the DB.
              bookDao.addBooks(mergedBookEntities)

          }
      )
    }

    //updates and save the books favorite status
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

    fun getBooksBySubjectCached(subject: String, forceNewBooks: Boolean): LiveData<Resource<List<Book>>>  {
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
                    //save the books from the api while keeping the saved user data of each book avoid overwriting with the api
                    val existingBookEntity = bookDao.getBookByIdSuspend(apiBook.key ?: "")
                    if(existingBookEntity?.isTranslated == true && !forceNewBooks){
                        Log.d("cacheRepo", " not overwritting book: ${existingBookEntity.title}")
                        existingBookEntity
                    }else {
                        apiBook.toBook(
                            existingIsFavorite = existingBookEntity?.isFavorite,
                            existingRating = existingBookEntity?.rating,
                            existingReview = existingBookEntity?.review,
                            existingSubject = subject
                        )
                    }
                }
                bookDao.addBooks(mergedBookEntities)
                //if the language is hebrew, enqueue translateWorker
                if (langProvider.isAppLanguageHebrew()) {
                    val booksToTranslateIds = mergedBookEntities
                        .filter { !it.isTranslated }
                        .map { it.id }
                        .toTypedArray()

                    if (booksToTranslateIds.isNotEmpty()) {
                        val workRequest = OneTimeWorkRequestBuilder<ListTranslationWorker>()
                            .setInputData(workDataOf("bookIds" to booksToTranslateIds))
                            .build()

                        workManager.enqueue(workRequest)
                        Log.d("BookRepo", "Enqueued ListTranslationWorker for ${booksToTranslateIds.size} books in subject $subject.")
                    } else {
                        Log.d("BookRepo", "No untranslated books found to enqueue for subject $subject.")
                    }
                }

            }
        )
    }

    //get all the books by subject, wrapped in resource to emit the all of the subjects status
    fun getBooksGroupedBySubjects(subjects: List<String>, forceNewBooks: Boolean): LiveData<Resource<List<BookCategory>>> {
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
            val subjectLiveData = getBooksBySubjectCached(subject, forceNewBooks) // Get LiveData<Resource<List<Book>>> for each subject
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
