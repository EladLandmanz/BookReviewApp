//package com.example.bookreviewapp
//
//import androidx.lifecycle.LiveData
//import com.example.bookreviewapp.dao.BookDao
//import com.example.bookreviewapp.data.OpenLibraryApi
//import com.example.bookreviewapp.entities.Book
//import javax.inject.Inject
//import javax.inject.Singleton
//import javax.sql.DataSource
//
//@Singleton
//class BookRepository @Inject constructor(
//    private val openLibraryApi: OpenLibraryApi,
//    private val bookDao: BookDao
//){
//
//    suspend fun fetchBookFromApi(bookId: String): BookResponse {
//        return openLibraryApi.getBookDetails(bookId)
//    }
//
//    fun getAllBooks(): LiveData<List<Book>> = bookDao.getAllBooks()
//
//    fun getBookByTitle(title: String): LiveData<Book> = bookDao.getBookByTitle(title)
//
//    fun getBookFromDb(bookId: String): LiveData<Book> = bookDao.getBookById(bookId)
//
//    fun getRecommendedBooks(): LiveData<List<Book>> = bookDao.getTopRatedBooks()
//
//    suspend fun addBook(book: Book) {
//        bookDao.addBook(book)
//    }
//
//    suspend fun deleteBook(book: Book) {
//        bookDao.deleteBook(book)
//    }
//
//    suspend fun updateBook(book: Book) {
//        bookDao.updateBook(book)
//    }
//
//}