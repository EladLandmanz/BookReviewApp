package com.example.bookreviewapp.UI.favorites

import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.bookreviewapp.Utils.Loading
import com.example.bookreviewapp.Utils.Resource
import com.example.bookreviewapp.Utils.Success
import com.example.bookreviewapp.data.BookCategory
import com.example.bookreviewapp.data.BookRepository
import com.example.bookreviewapp.data.SearchBook
import com.example.bookreviewapp.entities.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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