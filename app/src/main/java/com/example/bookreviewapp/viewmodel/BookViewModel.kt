package com.example.bookreviewapp.viewmodel

import android.util.Log
import androidx.lifecycle.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.bookreviewapp.data.remote_db.BookCategory
import com.example.bookreviewapp.data.models.Book
import com.example.bookreviewapp.data.repositories.BookRepository
import com.example.bookreviewapp.data.remote_db.SearchBook
import com.example.bookreviewapp.data.workers.ListTranslationWorker
import com.example.bookreviewapp.utils.LangProvider
import com.example.bookreviewapp.utils.Resource
import com.example.bookreviewapp.utils.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    private val langProvider: LangProvider,
    private val workManager: WorkManager,
    private val repository: BookRepository
) : ViewModel() {

    private var translationWorkEnqueued = false
    private val _triggerFetchBooks = MutableLiveData<Unit>()

    val trendingBooks : LiveData<Resource<List<Book>>> = _triggerFetchBooks.switchMap {
        Log.d("bookViewModel", "entered switchmap")
        val forceNewBook = !langProvider.isAppLanguageHebrew()
        repository.withCacheGetTrendingBooks(forceNewBook).map { resource ->
            //if the resource is status is successful and the list of books isnt empty, continue te check if translation is needed
            if (resource.status is Success && !resource.status.data.isNullOrEmpty()) {
                // Check if the app language is Hebrew and we haven't enqueued this work yet
                if (langProvider.isAppLanguageHebrew() && !translationWorkEnqueued) {
                    enqueueTranslationWorker(resource.status.data)
                    translationWorkEnqueued = true // Set the flag
                }
            }
            resource
        }
    }

    fun fetchTrendingBooks() {
        //triggers the switchMap
        _triggerFetchBooks.value = Unit
    }

    private var currentQuery: String = ""

    private val _subjectBooks = MutableLiveData<List<BookCategory>>()
    val subjectBooks: LiveData<List<BookCategory>> = _subjectBooks

    // Internal mutable LiveData (can be changed from inside ViewModel)
    private val _books = MutableLiveData<List<Book>>()

    // External read-only LiveData (observed by the Fragment)
    val books: LiveData<List<Book>> = _books

    fun searchBooks(query: String) {
        currentQuery = query

        viewModelScope.launch {
            try {
                val response = repository.searchBooks(query)
                val limited = response.docs.take(10)
                _books.value = limited.map { searchBookToBook(it) }
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error searching books: ${e.message}", e)
            }
        }
    }

    private fun enqueueTranslationWorker(books: List<Book>){
        val booksToTranslate = books.map { it.id }.toTypedArray()
        val workRequest = OneTimeWorkRequestBuilder<ListTranslationWorker>()
            .setInputData(workDataOf("bookIds" to booksToTranslate))
            .build()
        workManager.enqueue(workRequest)
    }

    private fun searchBookToBook(searchBook: SearchBook): Book =
        Book(
            id = searchBook.key ?: "",
            title = searchBook.title ?: "No title",
            author = searchBook.author_name?.firstOrNull() ?: "Unknown author",
            rating = searchBook.edition_count?.toFloat() ?: 0f,
            summary = "",
            imageUrl = searchBook.cover_i?.let {
                "https://covers.openlibrary.org/b/id/${it}-M.jpg"
            } ?: ""
        )
}
