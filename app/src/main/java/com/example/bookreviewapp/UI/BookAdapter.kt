package com.example.bookreviewapp.UI

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookreviewapp.Book
import com.example.bookreviewapp.databinding.BookItemBinding

class BookAdapter(private var books: MutableList<Book>, private val callback: BooksListener) :
    RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    interface BooksListener {
        fun onItemClicked(position: Int)
        fun onItemLongClicked(position: Int)
    }

    inner class BookViewHolder(val binding: BookItemBinding) :
        RecyclerView.ViewHolder(binding.root), OnClickListener, View.OnLongClickListener{
        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }

        override fun onClick(p0: View?) {
            callback.onItemClicked(adapterPosition)
        }

        override fun onLongClick(p0: View?): Boolean {
            callback.onItemLongClicked(adapterPosition)
            return true
        }
        }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = BookItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.binding.titleTextView.text = book.title
        holder.binding.authorTextView.text = book.author
        holder.binding.ratingTextView.text = book.rating.toString()

        Glide.with(holder.binding.root.context)
            .load(book.imageUrl)
            .into(holder.binding.bookImageView)

        Log.d("BookAdapter", "Binding book: ${book.title}")

    }
    fun updateBooks(newBooks: List<Book>) {
        books = newBooks.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount() = books.size
}
