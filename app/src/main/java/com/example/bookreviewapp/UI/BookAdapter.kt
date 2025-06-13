package com.example.bookreviewapp.UI

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookreviewapp.Book
import com.example.bookreviewapp.databinding.BookCardBinding


class BookAdapter(private val books:List<Book>, private val callback : BookListener): RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    interface BookListener  {
        fun onBookClicked(index: Int)
        fun onBookLongClick(index: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder =
       BookViewHolder(BookCardBinding.inflate(LayoutInflater.from(parent.context), parent ,false))


    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount(): Int = books.size


    inner class BookViewHolder(private val binding: BookCardBinding) : RecyclerView.ViewHolder(binding.root),
    View.OnClickListener, View.OnLongClickListener {
        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }

        override fun onClick(p0: View?) {
            callback.onBookClicked(adapterPosition)
        }

        override fun onLongClick(p0: View?): Boolean {
            callback.onBookLongClick(adapterPosition)
            return true
        }

        fun bind (book:Book){
            binding.bookTitle.text = book.title
            binding.bookAuthor.text = book.author
            binding.bookDescription.text = book.summary
            Glide.with(binding.root).load(book.imageUrl).circleCrop().into(binding.bookImage)

        }

    }
}