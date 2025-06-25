package com.example.bookreviewapp.data.local_db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bookreviewapp.data.dao.BookDao
import com.example.bookreviewapp.data.models.Book
import kotlin.concurrent.Volatile

@Database(entities = [Book::class], version = 2)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var instance: BookDatabase? = null

        fun getDataBase(context: Context): BookDatabase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    BookDatabase::class.java,
                    "book_database"
                ).fallbackToDestructiveMigration().build()
                instance = newInstance
                newInstance
            }
        }
    }
}