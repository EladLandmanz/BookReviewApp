package com.example.bookreviewapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.bookreviewapp.utils.Resource
import com.example.bookreviewapp.data.repositories.BookRepository
import com.example.bookreviewapp.data.models.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {
    private val _triggerFetchFavs = MutableLiveData<Unit>()

    val favoriteBooks : LiveData<Resource<List<Book>>> = _triggerFetchFavs.switchMap {
        repository.withCacheGetFavoriteBooks()
    }

    fun fetchFavoriteBooks() {
        _triggerFetchFavs.value = Unit
    }

}