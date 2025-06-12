package com.example.bookreviewapp.UI

import android.util.Log
import androidx.lifecycle.*
import com.example.bookreviewapp.Book
import com.example.bookreviewapp.data.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    // Internal mutable LiveData (can be changed from inside ViewModel)
    private val _books = MutableLiveData<List<Book>>()

    // External read-only LiveData (observed by the Fragment)
    val books: LiveData<List<Book>> = _books

    // Fetches books using Coroutine
    fun fetchBooks() {
        viewModelScope.launch {
            Log.d("BookViewModel", "Fetching books...")

            try {
                val response = repository.getTrendingBooks()
                val sorted = response.docs
                    .sortedByDescending { it.edition_count ?: 0 }
                    .take(10)

                Log.d("BookViewModel", "API response: $response")

                _books.value = sorted.map { searchBook ->
                    Book(
                        id = searchBook.key?.hashCode() ?: 0,
                        title = searchBook.title ?: "No title",
                        author = searchBook.author_name?.firstOrNull() ?: "Unknown author",
                        rating = searchBook.edition_count?.toFloat() ?: 0f,
                        summary = "",
                        imageUrl = searchBook.cover_i?.let {
                            "https://covers.openlibrary.org/b/id/${it}-M.jpg"
                        } ?: ""
                    )
                }


            } catch (e: Exception) {
                Log.e("BookViewModel", "Error fetching books: ${e.message}", e)
            }

        }
    }


}
