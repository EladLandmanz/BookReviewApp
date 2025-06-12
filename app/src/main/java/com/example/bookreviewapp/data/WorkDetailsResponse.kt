package com.example.bookreviewapp.data

data class WorkDetailsResponse(
    val title: String?,
    val description: Description?,
    val covers: List<Int>?,
    val authors: List<AuthorRef>?
)

data class AuthorRef(
    val author: AuthorKey
)

data class AuthorKey(
    val key: String

)

data class Description(
    val value: String?
)