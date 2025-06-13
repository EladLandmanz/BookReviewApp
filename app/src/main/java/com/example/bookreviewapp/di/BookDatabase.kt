package com.example.bookreviewapp.di

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bookreviewapp.dao.BookDao
import com.example.bookreviewapp.entities.Book

@Database(entities = [Book::class], version = 1)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}