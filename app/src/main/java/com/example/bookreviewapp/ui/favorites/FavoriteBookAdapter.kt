package com.example.bookreviewapp.ui.favorites

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookreviewapp.data.models.Book
import com.example.bookreviewapp.databinding.BookItemBinding

class FavoriteBookAdapter (private val callback: BookListener) :
    ListAdapter<Book, FavoriteBookAdapter.FavoriteBookViewHolder>(BookDiffCallback()) {

        interface BookListener {
            fun onItemClick(book: Book)
            fun onItemLongClick(book: Book)
        }

        inner class FavoriteBookViewHolder(val binding: BookItemBinding) :
            RecyclerView.ViewHolder(binding.root),
        View.OnClickListener, View.OnLongClickListener{

            init {
                binding.root.setOnClickListener(this)
                binding.root.setOnLongClickListener(this)

            }

            override fun onClick(v: View?) {
                callback.onItemClick(getItem(adapterPosition))
            }

            override fun onLongClick(v: View?): Boolean {
                callback.onItemLongClick(getItem(adapterPosition))
                return true
            }

            fun bind(book : Book){
                binding.titleTextView.text = book.title
                binding.authorTextView.text = book.author
                binding.ratingTextView.text = book.rating.toString()

                Glide.with(binding.root.context)
                    .load(book.imageUrl)
                    .into(binding.bookImageView)

                Log.d("BookAdapter", "Binding book: ${book.title}")

            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteBookViewHolder {
            val binding = BookItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return FavoriteBookViewHolder(binding)
        }

        override fun onBindViewHolder(holder: FavoriteBookViewHolder, position: Int) {
            val book = getItem(position) // <-- Get the item from ListAdapter's internal data
            Log.d("BookAdapter", "onBindViewHolder: Binding position $position, Book ID: ${book.id}, Title: ${book.title}")
            holder.bind(book) // Pass the book to your ViewHolder's bind method
        }

    private class BookDiffCallback : DiffUtil.ItemCallback<Book>() {

        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id // Books are the same if their IDs match
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem // Data classes automatically generate equals() based on all properties
        }
    }
    }

