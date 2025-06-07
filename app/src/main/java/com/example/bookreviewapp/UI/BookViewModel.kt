package com.example.bookreviewapp.UI

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
            try {
                val response = repository.getMostPublishedBooks()
                _books.value = response.docs.map { apiBook ->
                    Book(
                        id = apiBook.key.hashCode(),
                        title = apiBook.title,
                        author = apiBook.author_name?.getOrNull(0) ?: "Unknown author",
                        rating = apiBook.edition_count.toFloat(),
                        summary = "",
                        imageUrl = "https://covers.openlibrary.org/b/olid/${
                            apiBook.key.removePrefix("/works/")
                        }-M.jpg"
                    )
                }
            } catch (e: Exception) {
                _books.value = null // Triggers error UI in the Fragment
            }
        }
    }
}
