package com.example.bookreviewapp.UI.favorites

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper.Callback
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookreviewapp.Book
import com.example.bookreviewapp.databinding.BookItemBinding

class FavoriteBookAdapter (private var books: MutableList<Book>, private val callback: BookListner) :
    RecyclerView.Adapter<FavoriteBookAdapter.FavoriteBookViewHolder>() {

        interface BookListner {
            fun onItemClick(index:Int)
            fun onItemLongClick(index:Int)
        }

        inner class FavoriteBookViewHolder(val binding: BookItemBinding) :
            RecyclerView.ViewHolder(binding.root),
        View.OnClickListener, View.OnLongClickListener{

            init {
                binding.root.setOnClickListener(this)
                binding.root.setOnLongClickListener(this)

            }

            override fun onClick(v: View?) {
                callback.onItemClick(adapterPosition)
            }

            override fun onLongClick(v: View?): Boolean {
                callback.onItemLongClick(adapterPosition)
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

        override fun onBindViewHolder(holder: FavoriteBookViewHolder, position: Int) { holder.bind(books[position]) }

        fun updateBooks(newBooks: List<Book>) {
            books = newBooks.toMutableList()
            notifyDataSetChanged()
        }

        override fun getItemCount() = books.size
    }

