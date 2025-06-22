package com.example.bookreviewapp.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.bookreviewapp.data.models.Book
import com.example.bookreviewapp.data.repositories.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    //private val _booksWithReviews = MutableLiveData<List<Book>>()
    val booksWithReviews = repository.getBooksWithReviews()




}