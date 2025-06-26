package com.example.bookreviewapp.utils

import android.util.Log
import com.example.bookreviewapp.data.models.Book
import com.example.bookreviewapp.data.remote_db.SearchBook
import com.example.bookreviewapp.data.remote_db.SubjectResponse.SubjectBook
import com.example.bookreviewapp.data.remote_db.WorkDetailsResponse


fun SearchBook.toBook(
    existingIsFavorite: Boolean? = null,
    existingRating: Float? = null,
    existingSubject: String? = null,
    existingTrending: Boolean? = null): Book {
    return Book(
            id = this.key ?: "",
            title = this.title ?: "No title",
            author = this.author_name?.firstOrNull() ?: "Unknown author",
            summary = "",
            rating = existingRating?: 0.0f,
            isFavorite = existingIsFavorite ?: false,
            isTrending = existingTrending ?: false,
            subject = existingSubject,
            imageUrl = this.cover_i?.let {
                "https://covers.openlibrary.org/b/id/${it}-M.jpg"
            } ?: ""
        )
}

fun SubjectBook.toBook(
    existingIsFavorite: Boolean? = null,
    existingRating: Float? = null,
    existingSubject: String? = null,
    existingTrending: Boolean? = null):Book{
    return Book(
        id = this.key ?: "",
        title = this.title ?: "No title",
        author = this.authors?.firstOrNull()?.name ?: "Unknown author",
        rating = existingRating?: 0.0f,
        summary = "",
        subject = existingSubject,
        isFavorite = existingIsFavorite ?: false,
        isTrending = existingTrending?: false,
        imageUrl = this.cover_id?.let { id ->
            "https://covers.openlibrary.org/b/id/${id}-M.jpg"
        } ?: ""
    )
}

fun WorkDetailsResponse.mapWorkToBook(
    id: String,
    existingIsFavorite: Boolean? = null,
    existingRating: Float? = null,
    existingSubject: String? = null,
    existingTrending: Boolean? = null): Book {
    val imageUrl = this.covers?.firstOrNull()?.let {
        "https://covers.openlibrary.org/b/id/$it-L.jpg"
    }

    val title = this.title.toString()
    val summary = when (this.description) {
        is String -> this.description as String
        is Map<*, *> -> (this.description as Map<*, *>)["value"] as? String
        else -> null
    }
    val authorId = this.authors?.firstOrNull()?.author?.key.toString()
    Log.d("workMap", "title ${title}, id ${id}")
    return Book(
        id = id,
        title = title,
        summary = summary,
        imageUrl = imageUrl,
        rating = existingRating?: 0.0f,
        author = this.authors?.firstOrNull()?.author?.key ?: "Unknown author",
        subject = existingSubject,
        isTrending = existingTrending?: false,
        isFavorite = existingIsFavorite ?: false
    )
}