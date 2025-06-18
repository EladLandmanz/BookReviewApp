package com.example.bookreviewapp.UI

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookreviewapp.data.BookCategory
import com.example.bookreviewapp.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val categories: MutableList<BookCategory>,
    private val listener: BookAdapter.BooksListener
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCategoryBinding.inflate(inflater, parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.categoryTitle.text = category.subject.capitalize()

        val bookAdapter = BookAdapter(category.books.toMutableList(), listener)
        holder.binding.innerRecyclerView.layoutManager =
            LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.binding.innerRecyclerView.adapter = bookAdapter
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<BookCategory>) {
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }

}
