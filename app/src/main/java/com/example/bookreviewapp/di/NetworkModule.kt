package com.example.bookreviewapp.di

import android.provider.SyncStateContract.Constants
import com.example.bookreviewapp.data.OpenLibraryApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "https://openlibrary.org/"
    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson) : Retrofit {
        return Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
    }

    @Provides
    fun provideGson() : Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOpenLibraryApi(retrofit: Retrofit): OpenLibraryApi {
        return retrofit.create(OpenLibraryApi::class.java)
    }

}