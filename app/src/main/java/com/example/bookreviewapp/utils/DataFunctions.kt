package com.example.bookreviewapp.utils

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import kotlinx.coroutines.Dispatchers

fun <T,A> performFetchingAndSaving(localDbFetch: () -> LiveData<T>,
                                   remoteDbFetch: suspend () ->Resource<A>,
                                   localDbSave: suspend (A) -> Unit) : LiveData<Resource<T>> =

    liveData(Dispatchers.IO) {
        Log.d("PFAS", "emitting loading")
        emit(Resource.loading())

        val source = localDbFetch().map { Resource.success(it) }
        emitSource(source)
        Log.d("PFAS", "emitting localDB")
        val fetchResource = remoteDbFetch()

        if(fetchResource.status is Success) {
            Log.d("PFAS", "saving localDB")
            localDbSave(fetchResource.status.data!!)
        }

        else if(fetchResource.status is Error){
            emit(Resource.error(fetchResource.status.message))
            emitSource(source)
        }
    }