package com.example.bookreviewapp.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookreviewapp.data.models.Book
import com.example.bookreviewapp.databinding.BookItemBinding

class ReviewBookAdapter(private var books: List<Book>, private val callback: ReviewBooksListener) :
    RecyclerView.Adapter<ReviewBookAdapter.ReviewBookViewHolder>() {

    interface ReviewBooksListener {
        fun onItemClicked(book: Book)
        fun onItemLongClicked(book: Book)
    }

    inner class ReviewBookViewHolder(val binding: BookItemBinding) :
        RecyclerView.ViewHolder(binding.root),
        OnClickListener, View.OnLongClickListener{

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)

        }

        override fun onClick(v: View?) {
            callback.onItemClicked(books[adapterPosition])
        }

        override fun onLongClick(v: View?): Boolean {
            callback.onItemLongClicked(books[adapterPosition])
            return true
        }

        fun bind(book : Book){
            binding.titleTextView.text = book.title
            binding.authorTextView.visibility = View.GONE
            binding.favoriteIcon.visibility = View.GONE
            binding.reviewText.visibility = View.VISIBLE
            binding.reviewText.text = book.review ?: ""


            Glide.with(binding.root.context)
                .load(book.imageUrl)
                .into(binding.bookImageView)

            Log.d("BookAdapter", "Binding book: ${book.title}")

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewBookViewHolder {
        val binding = BookItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReviewBookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewBookViewHolder, position: Int) {
        val book = books[position]
        holder.bind(book)
        Log.d("Adapter", "review: ${book.review}")
    }

    override fun getItemCount(): Int = books.size

    // updates the list of books
    fun updateData(newBooks: List<Book>) {
        Log.d("reviewAdapter", "entered update data")
        this.books = newBooks
        notifyDataSetChanged()
    }

    // to get a book by its position
    fun getBookAt(position: Int): Book = books[position]
}