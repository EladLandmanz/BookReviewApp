package com.example.bookreviewapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.bookreviewapp.data.models.Book
import com.example.bookreviewapp.data.repositories.BookRepository
import com.example.bookreviewapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    private val _triggerSearchBooks = MutableLiveData<String>()

    val resultBooks : LiveData<Resource<List<Book>>> = _triggerSearchBooks.switchMap {
        repository.withLoadingSearchBooks(_triggerSearchBooks.value?.toString() ?: "")
    }

    fun searchBooks(query: String) {
        _triggerSearchBooks.value = query
    }


}