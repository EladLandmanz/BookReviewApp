package com.example.bookreviewapp.data

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bookreviewapp.dao.BookDao
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation
import dagger.assisted.Assisted
//import com.google.firebase.functions.dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await


@HiltWorker
class TranslationWorker @AssistedInject constructor(
    @Assisted  appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val bookDao: BookDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {

        val bookId = inputData.getString("bookId") ?: return Result.failure()
        Log.d("TranslationWorker", "Started for bookId: $bookId")
        val book = bookDao.getBookByIdSuspend(bookId)
        if (book == null) {
            Log.e("TranslationWorker", "Book not found in DB")
            return Result.failure()
        }

        try {
            val translatedTitle = translateText(book.title)
            val translatedSummary = translateText(book.summary ?: "")

            val translatedBook = book.copy(
                title = translatedTitle,
                summary = translatedSummary
            )

            bookDao.updateBook(translatedBook)
            Log.d("TranslationWorker", "Book translated and updated successfully")

            return Result.success()
        } catch (e: Exception) {
            Log.e("TranslationWorker", "Translation failed: ${e.message}", e)
            return Result.failure()
        }
    }

    private suspend fun translateText(text: String): String {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage("en")
            .setTargetLanguage("iw")
            .build()

        val translator = Translation.getClient(options)

        translator.downloadModelIfNeeded().await()

        val translated = translator.translate(text).await()
        translator.close()
        return translated
    }
}