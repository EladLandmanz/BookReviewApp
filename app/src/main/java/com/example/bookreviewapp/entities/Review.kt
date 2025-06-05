package com.example.bookreviewapp.entities
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "review_id")
    val reviewId: Int = 0,

    @ColumnInfo(name = "book_id")
    val bookId: Int,

    @ColumnInfo(name = "reviewer_name")
    val reviewerName: String,

    @ColumnInfo(name = "rating")
    val rating: Int,

    @ColumnInfo(name = "review_text")
    val reviewText: String,

    @ColumnInfo(name = "date")
    val date: Date
)
