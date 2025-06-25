package com.example.bookreviewapp.data.remote_db

data class SubjectResponse(
    val works: List<SubjectBook>
) {
    data class SubjectBook(
        val key: String?,
        val title: String?,
        val authors: List<Author>?,
        val edition_count: Int?,
        val cover_id: Int?
    )

    data class Author(
        val name: String?
    )
}
