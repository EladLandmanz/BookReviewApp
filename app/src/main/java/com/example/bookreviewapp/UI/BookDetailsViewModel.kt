package com.example.bookreviewapp.UI

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.bookreviewapp.data.BookRepository
import com.example.bookreviewapp.data.TranslationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.bookreviewapp.entities.Book
import dagger.hilt.android.internal.Contexts.getApplication

@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    application: Application,
    private val repository: BookRepository
) : AndroidViewModel(application) {
    private val _book = MutableLiveData<Book?>()
    val book: LiveData<Book?> = _book

    private fun isAppLanguageHebrew(): Boolean {
        val locale = getApplication<Application>().resources.configuration.locales[0]
        return locale.language == "iw" || locale.language == "he"
    }

    fun loadBook(bookId: String) {
        viewModelScope.launch {
            val cleanBookId = bookId.removePrefix("/works/")
            Log.d("load","the clean id: ${cleanBookId}")
            val localBook = repository.getBookByIdSuspend(cleanBookId)

            if (localBook != null){
                Log.d("load","local book isn't null")
                _book.value = localBook

            }
            else{
                try {
                    val response = repository.fetchBookFromApi(cleanBookId)
                    val newBook = repository.mapWorkDetailsToBook(cleanBookId, response)
                    Log.d("BookDetailsVM", "Saving book with id: ${newBook.id}")
                    val bookId = newBook.id
                    repository.addBook(newBook)
                    _book.value = newBook

                    if (isAppLanguageHebrew()) {
                        val workRequest = OneTimeWorkRequestBuilder<TranslationWorker>()
                            .setInputData(workDataOf("bookId" to bookId))
                            .build()

                        WorkManager.getInstance(getApplication())
                            .enqueue(workRequest)
                        Log.d("TranslationWork", "Work enqueued for bookId: ${newBook.id}")
                    }

                } catch (e: Exception) {
                    Log.e("BookDetailsVM", "API Error: ${e.message}")
                }
            }

        }
    }

    fun toggleFavorite(book: Book) {
        viewModelScope.launch {
            book.isFavorite = !book.isFavorite
            repository.updateBook(book)
            _book.value = book
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            repository.updateBook(book)
        }
    }


}