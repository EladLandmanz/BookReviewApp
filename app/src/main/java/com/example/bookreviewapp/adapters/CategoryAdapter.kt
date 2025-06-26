package com.example.bookreviewapp.adapters

import android.util.Log
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
        Log.d("CategoryAdapter", "onCreateViewHolder: Creating new CategoryViewHolder.")
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        Log.d("CategoryAdapter", "onBindViewHolder: Binding position $position, Category: ${category.subject}")
        // Map subject keys to string resource IDs
        val subjectMap = mapOf(
            "fantasy" to com.example.bookreviewapp.R.string.subject_fantasy,
            "romance" to com.example.bookreviewapp.R.string.subject_romance,
            "history" to com.example.bookreviewapp.R.string.subject_history,
            "mystery" to com.example.bookreviewapp.R.string.subject_mystery,
            "horror" to com.example.bookreviewapp.R.string.subject_horror
        )

        val resId = subjectMap[category.subject.lowercase()]
        val localizedSubject = resId?.let { holder.itemView.context.getString(it) } ?: category.subject

        holder.binding.categoryTitle.text = localizedSubject

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
