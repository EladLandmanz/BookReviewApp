package com.example.bookreviewapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookreviewapp.data.models.Book
import com.example.bookreviewapp.databinding.ItemBookReviewBinding

class ReviewBookAdapter(private var books: List<Book>) :
    RecyclerView.Adapter<ReviewBookAdapter.ReviewBookViewHolder>() {

    inner class ReviewBookViewHolder(val binding: ItemBookReviewBinding) :
        RecyclerView.ViewHolder(binding.root)

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
}