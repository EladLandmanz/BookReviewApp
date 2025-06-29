package com.example.bookreviewapp.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookreviewapp.data.models.Book
import com.example.bookreviewapp.databinding.ItemBookReviewBinding

class ReviewBookAdapter(private var books: List<Book>, private val callback: ReviewBooksListener) :
    RecyclerView.Adapter<ReviewBookAdapter.ReviewBookViewHolder>() {

    interface ReviewBooksListener {
        fun onItemClicked(book: Book)
        fun onItemLongClicked(book: Book)
    }


    inner class ReviewBookViewHolder(val binding: ItemBookReviewBinding) :
        RecyclerView.ViewHolder(binding.root), OnClickListener, View.OnLongClickListener {
        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }

        override fun onClick(p0: View?) {
            callback.onItemClicked(books[adapterPosition])
        }

        override fun onLongClick(p0: View?): Boolean {
            callback.onItemLongClicked(books[adapterPosition])
            return true
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewBookViewHolder {
        val binding = ItemBookReviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReviewBookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewBookViewHolder, position: Int) {
        val book = books[position]
        holder.binding.reviewBookTitle.text = book.title
        holder.binding.reviewText.text = book.review ?: ""
        Glide.with(holder.itemView.context)
            .load(book.imageUrl)
            .into(holder.binding.reviewBookImage)
    }

    override fun getItemCount(): Int = books.size

    fun updateData(newBooks: List<Book>) {
        Log.d("reviewAdapter", "entered update data")
        this.books = newBooks
        notifyDataSetChanged()
    }

    fun getBookAt(position: Int): Book = books[position]
}