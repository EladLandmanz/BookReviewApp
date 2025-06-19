package com.example.bookreviewapp.UI.favorites

import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookreviewapp.Book
import com.example.bookreviewapp.data.BookCategory
import com.example.bookreviewapp.data.BookRepository
import com.example.bookreviewapp.data.SearchBook
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {
    private var currentQuery: String = ""

    private val _subjectBooks = MutableLiveData<List<BookCategory>>()
    val subjectBooks: LiveData<List<BookCategory>> = _subjectBooks

    // External read-only LiveData (observed by the Fragment)
    val favoriteBooks : LiveData<List<com.example.bookreviewapp.entities.Book>> = repository.getAllFavoriteBooks()


    // Fetches books using Coroutine
    /*
    fun fetchBooks() {
        viewModelScope.launch {
            Log.d("BookViewModel", "Fetching books...")

            try {
                val response = repository.getAllFavoriteBooks()
                val sorted = response.docs
                    .sortedByDescending { it.edition_count ?: 0 }
                    .take(10)

                Log.d("BookViewModel", "API response: $response")

                _books.value = sorted.map { searchBook ->
                    Book(
                        id = searchBook.key?: "",
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
    }*/
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


    fun fetchBooksGroupedBySubjects(subjects: List<String>) {
        viewModelScope.launch {
            val result = mutableListOf<BookCategory>()
            for (subject in subjects) {
                try {
                    val response = repository.getBooksBySubject(subject)
                    val books = response.works.take(5).map {
                        Book(
                            id = it.key ?: "",
                            title = it.title ?: "No title",
                            author = it.authors?.firstOrNull()?.name ?: "Unknown author",
                            rating = it.edition_count?.toFloat() ?: 0f,
                            summary = "",
                            imageUrl = it.cover_id?.let { id ->
                                "https://covers.openlibrary.org/b/id/${id}-M.jpg"
                            } ?: ""
                        )
                    }
                    result.add(BookCategory(subject, books))
                } catch (_: Exception) {}
            }
            _subjectBooks.value = result
        }
    }



    fun getFilteredBooks(): List<Book> {
        val queryLower = currentQuery.lowercase()
        return _books.value?.filter { book ->
            book.title.lowercase().contains(queryLower) ||
                    book.author.lowercase().contains(queryLower)
        } ?: emptyList()
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