package com.example.bookreviewapp.di

import android.content.Context
import androidx.room.Room
import com.example.bookreviewapp.dao.BookDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context)
    : BookDatabase {
        return Room.databaseBuilder(
            appContext,
            BookDatabase::class.java,
            "book_database"
        ).build()
    }

    @Provides
    fun provideBookDao(database: BookDatabase): BookDao {
        return database.bookDao()
    }
}