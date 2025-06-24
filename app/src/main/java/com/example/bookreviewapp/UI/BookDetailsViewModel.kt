package com.example.bookreviewapp.UI

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.bookreviewapp.Utils.Resource
import com.example.bookreviewapp.data.BookRepository
import com.example.bookreviewapp.data.TranslationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.bookreviewapp.entities.Book
import dagger.hilt.android.internal.Contexts.getApplication
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    application: Application,
    private val repository: BookRepository
) : AndroidViewModel(application) {
   // private val _book = MutableLiveData<Book?>()
   // val book: LiveData<Book?> = _book

    ////////////////////try a new approach

    private val _bookId = MutableLiveData<String>()

    val bookResource: LiveData<Resource<Book>> = _bookId.switchMap { bookId ->
        if (bookId.isNullOrEmpty()) {
            MutableLiveData(Resource.error("Book ID is missing", null))
        } else {
            repository.withCacheGetBookDetails(bookId)
        }
    }

    private fun isAppLanguageHebrew(): Boolean {
        val locale = getApplication<Application>().resources.configuration.locales[0]
        return locale.language == "iw" || locale.language == "he"
    }

    fun loadBook(bookId: String) {
        viewModelScope.launch {
            val cleanBookId = bookId.removePrefix("/works/")
            Log.d("load", "the clean id: ${cleanBookId}")

            _bookId.value = cleanBookId //trigger the switchMap and update the data


            if (isAppLanguageHebrew() && cleanBookId.isNotEmpty()) {
                val workRequest = OneTimeWorkRequestBuilder<TranslationWorker>()
                    .setInputData(workDataOf("bookId" to bookId))
                    .build()

                WorkManager.getInstance(getApplication())
                    .enqueue(workRequest)
                Log.d("TranslationWork", "Work enqueued for bookId: ${cleanBookId}")
            }

        }
    }

    fun toggleFavorite() {
        val currentBookResource = bookResource.value
        val currentBook = currentBookResource?.status?.data

        if (currentBook == null) {
            Log.w("BookDetailsVM", "Cannot toggle favorite: book data is null.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) { // Perform DB operation on IO dispatcher
            val newFavoriteStatus = !currentBook.isFavorite
            Log.d("BookDetailsVM", "Toggling favorite for ${currentBook.id} to $newFavoriteStatus")


            val updatedBook = currentBook.copy(isFavorite = newFavoriteStatus)

            val success = repository.updateBookFavoriteStatus(updatedBook.id, updatedBook.isFavorite)

            if (!success) {
                Log.e("BookDetailsVM", "Failed to save favorite status for ${currentBook.id}")
            }
        }
    }


    fun updateBook(book: Book) {
        viewModelScope.launch {
            repository.updateBook(book)
        }
    }


}