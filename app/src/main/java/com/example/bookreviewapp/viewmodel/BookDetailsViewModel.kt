package com.example.bookreviewapp.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.bookreviewapp.utils.Resource
import com.example.bookreviewapp.data.repositories.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import com.example.bookreviewapp.data.models.Book
import com.example.bookreviewapp.utils.Success

@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    application: Application,
    private val repository: BookRepository
) : AndroidViewModel(application) {

    private val _bookId = MutableLiveData<String>()
    private var translationWorkEnqueued = false

    val bookResource: LiveData<Resource<Book>> = _bookId.switchMap { bookId ->
        if (bookId.isNullOrEmpty()) {
            MutableLiveData(Resource.error("Book ID is missing", null))
        } else {
            //force a new book fetch if the language is english
            val forceNewBook = !isAppLanguageHebrew()

            Log.d("switchmap", "get book from repo")
            repository.withCacheGetBookDetails(bookId, forceNewBook).map { resource ->
                if (resource.status is Success && resource.status.data != null) {
                    //check if the book is not a place holder
                    val isRealBook = resource.status.data.id != "Loading"
                    // Check if the app language is Hebrew and we haven't enqueued this work yet
                    if (isAppLanguageHebrew() && !translationWorkEnqueued && isRealBook ) {
                        Log.d("switchmap", "in the if is hebrew")
                        enqueueTranslationWorker(resource.status.data.id)
                        translationWorkEnqueued = true // Set the flag
                    }
                }
                resource
            }
        }
    }

    private fun isAppLanguageHebrew(): Boolean {
        val locale = getApplication<Application>().resources.configuration.locales[0]
        return locale.language == "iw" || locale.language == "he"
    }

    fun loadBook(bookId: String) {
        translationWorkEnqueued = false
        viewModelScope.launch {
            val cleanBookId = bookId.removePrefix("/works/")
            Log.d("load", "the clean id: ${cleanBookId}")

            _bookId.value = cleanBookId //trigger the switchMap and update the data

//            if (isAppLanguageHebrew() && cleanBookId.isNotEmpty()) {
//                val workRequest = OneTimeWorkRequestBuilder<TranslationWorker>()
//                    .setInputData(workDataOf("bookId" to cleanBookId))
//                    .build()
//
//                WorkManager.getInstance(getApplication())
//                    .enqueue(workRequest)
//                Log.d("TranslationWorker", "Work enqueued for bookId: ${cleanBookId}")
//            }
        }
    }

    private fun enqueueTranslationWorker(bookId: String){
        val workRequest = OneTimeWorkRequestBuilder<TranslationWorker>()
            .setInputData(workDataOf("bookId" to bookId))
            .build()

        WorkManager.getInstance(getApplication())
            .enqueue(workRequest)
        Log.d("TranslationWorker", "Work enqueued for bookId: ${bookId}")

    }

    fun toggleFavorite() {
        val currentBookResource = bookResource.value
        val currentBook = currentBookResource?.status?.data

        if (currentBook == null) {
            Log.w("BookDetailsVM", "Cannot toggle favorite: book data is null.")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val newFavoriteStatus = !currentBook.isFavorite
            Log.d("BookDetailsVM", "Toggling favorite for ${currentBook.id} to $newFavoriteStatus")


            val updatedBook = currentBook.copy(isFavorite = newFavoriteStatus)

            val success =
                repository.updateBookFavoriteStatus(updatedBook.id, updatedBook.isFavorite)

            if (!success) {
                Log.e("BookDetailsVM", "Failed to save favorite status for ${currentBook.id}")
            }
        }
    }

    fun updateRating(rating: Float) {
        val currentBook = bookResource.value?.status?.data ?: return
        val updatedBook = currentBook.copy(rating = rating)

        viewModelScope.launch {
            repository.updateBook(updatedBook)
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            repository.updateBook(book)
        }
    }

    fun submitReview(review: String) {
            val currentBook = bookResource.value?.status?.data ?: return
            val updatedBook = currentBook.copy(review = review)

            viewModelScope.launch {
            repository.updateBook(updatedBook)
        }
    }
}