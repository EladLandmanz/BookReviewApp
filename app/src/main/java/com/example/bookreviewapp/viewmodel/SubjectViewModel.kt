package com.example.bookreviewapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.bookreviewapp.data.remote_db.BookCategory
import com.example.bookreviewapp.data.repositories.BookRepository
import com.example.bookreviewapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubjectsViewModel @Inject constructor( // Renamed to SubjectsViewModel
    private val repository: BookRepository
) : ViewModel() {

    // This will hold the final LiveData for the UI
    private val _subjectsToLoad = MutableLiveData<List<String>>()

    val subjectCategories: LiveData<Resource<List<BookCategory>>> = _subjectsToLoad.switchMap { subjects ->
        if (subjects.isEmpty()) {
            MutableLiveData(Resource.success(emptyList()))
        } else {
            repository.getBooksGroupedBySubjects(subjects)
        }
    }

    // Function to trigger the loading of subjects
    fun fetchBooksForSubjects(subjects: List<String>) {
        _subjectsToLoad.value = subjects
    }

}