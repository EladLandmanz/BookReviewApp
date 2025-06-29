package com.example.bookreviewapp.data.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bookreviewapp.data.dao.BookDao
import com.example.bookreviewapp.data.models.Book
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation
import dagger.assisted.Assisted
//import com.google.firebase.functions.dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await


@HiltWorker
class ListTranslationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val bookDao: BookDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val bookIds = inputData.getStringArray("bookIds") ?: return Result.failure()
        Log.d("ListTranslationWorker", "Started for bookId: $bookIds")
        if (bookIds.isEmpty()) {
            Log.e("ListTranslationWorker", "Book List is empty")
            return Result.success()
        }


        val options = TranslatorOptions.Builder()
            .setSourceLanguage("en")
            .setTargetLanguage("iw")
            .build()

        val translator = Translation.getClient(options)
        Log.d("ListTranslationWorker",  "starting on ${bookIds.size} books")
        try {
            translator.downloadModelIfNeeded().await()

            val translatedBooks = coroutineScope {
                //iterate over all the books in the list and translate them
                bookIds.map {  bookId ->
                    async{
                        val book = bookDao.getBookByIdSuspend(bookId)

                        if (book != null) {
                            val translatedTitle = translator.translate(book.title).await()
                            val translatedSummary = translator.translate(book.summary ?: "").await()
                            val translatedAuthor = translator.translate(book.author).await()

                            book.copy(
                                title = translatedTitle,
                                author = translatedAuthor,
                                summary = translatedSummary,
                                isTranslated = true
                            )
                        }else{
                            null
                        }
                    }
                }.awaitAll()
            }.filterNotNull()
            // Log.d("TranslationWorker",  "${book.title} translated to $translatedTitle and updated successfully")
                // Log.d("TranslationWorker", "book ${book}")
                //Log.d("TranslationWorker", " to ${translatedBook}")
                //Log.d("TranslationWorker", "og key ${book.id} to ${translatedBook.id}")
            if(translatedBooks.isNotEmpty()){
                bookDao.updateBooks(translatedBooks)
                Log.d("ListTranslationWorker",  "translated to and updated successfully")
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("ListTranslationWorker", "Translation failed: ${e.message}", e)
            return Result.failure()
        }finally {
            translator.close()
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