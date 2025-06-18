package com.example.bookreviewapp.UI.favorites

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.bookreviewapp.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {
}