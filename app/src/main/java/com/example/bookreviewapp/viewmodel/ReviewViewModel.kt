package com.example.bookreviewapp.viewmodel

import androidx.lifecycle.*
import com.example.bookreviewapp.data.repositories.BookRepository
import com.example.bookreviewapp.data.models.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    val booksWithReviews = repository.getBooksWithReviews()

    fun submitReview(book: Book, review: String) {
        viewModelScope.launch {
            book.review = review
            repository.updateBook(book)
        }
    }

}