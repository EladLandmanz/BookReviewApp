package com.example.bookreviewapp.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.bookreviewapp.entities.Review

interface ReviewDao {
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun addReview(review: Review)

    //delete an existing review
    @Delete
    fun deleteReview(vararg review: Review)

    //update an existing review
    @Update
    fun updateReview(review: Review)

    //get all reviews for a book by his id
    @Query("SELECT * FROM reviews WHERE book_id = :bookId")
    fun getReviewsForBook(bookId: Int): LiveData<List<Review>>

    //get the average rating for a book by his id
    @Query("SELECT AVG(rating) FROM reviews WHERE book_id = :bookId")
    fun getAverageRatingForBook(bookId: Int): LiveData<Float?>


}