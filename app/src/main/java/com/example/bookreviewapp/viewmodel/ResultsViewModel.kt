package com.example.bookreviewapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.bookreviewapp.data.models.Book
import com.example.bookreviewapp.data.repositories.BookRepository
import com.example.bookreviewapp.data.workers.ListTranslationWorker
import com.example.bookreviewapp.utils.LangProvider
import com.example.bookreviewapp.utils.Resource
import com.example.bookreviewapp.utils.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val langProvider: LangProvider,
    private val workManager: WorkManager,
    private val repository: BookRepository
) : ViewModel() {

    private val _triggerSearchBooks = MutableLiveData<String>()

    val resultBooks : LiveData<Resource<List<Book>>> = _triggerSearchBooks.switchMap {
        repository.withLoadingSearchBooks(_triggerSearchBooks.value?.toString() ?: "")
    }

    fun searchBooks(query: String) {
        Log.d("resultViewModel", " this q: $query")
        _triggerSearchBooks.value = query
    }

}