package com.example.bookreviewapp.viewmodel

import androidx.lifecycle.*
import com.example.bookreviewapp.data.repositories.BookRepository
import com.example.bookreviewapp.data.models.Book
import com.example.bookreviewapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    private val _triggerFetchReviews = MutableLiveData<Unit>()

    // the list of all books that have a review.
    val booksWithReviews : LiveData<List<Book>> = _triggerFetchReviews.switchMap {
        repository.getBooksWithReviews()
    }

    fun fetchBooksWithReviews() {
        _triggerFetchReviews.value = Unit
    }

    // update a book's review in the db
    fun submitReview(book: Book, review: String) {
        viewModelScope.launch {
            val updatedBook = book.copy(review = review)
            book.review = review
            repository.updateBook(updatedBook)
        }
    }

}