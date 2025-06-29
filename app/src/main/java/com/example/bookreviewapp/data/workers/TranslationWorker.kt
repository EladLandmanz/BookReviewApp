package com.example.bookreviewapp.data.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bookreviewapp.data.dao.BookDao
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation
import dagger.assisted.Assisted
//import com.google.firebase.functions.dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await


@HiltWorker
class TranslationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
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
            val translatedAuthor = translateText(book.author)

            val translatedBook = book.copy(
                title = translatedTitle,
                author = translatedAuthor,
                summary = translatedSummary,
                isTranslated = true
            )

            bookDao.updateBook(translatedBook)
            Log.d("TranslationWorker", "${book.title} translated to $translatedTitle and updated successfully")
            Log.d("TranslationWorker", "book ${book}")
            Log.d("TranslationWorker", " to ${translatedBook}")
            Log.d("TranslationWorker", "og key ${book.id} to ${translatedBook.id}")

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