package com.example.bookreviewapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookreviewapp.data.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.bookreviewapp.entities.Book

@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {
    private val _book = MutableLiveData<Book?>()
    val book: LiveData<Book?> = _book

    fun loadBook(bookId: String) {
        viewModelScope.launch {
            val localBook = repository.getBookFromDbSync(bookId)
            if (localBook != null){
                _book.value = localBook.value
            }
            else{
                try {
                    val response = repository.fetchBookFromApi(bookId)
                    val newBook = repository.mapWorkDetailsToBook(bookId, response)
                    repository.addBook(newBook)
                    _book.value = newBook
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