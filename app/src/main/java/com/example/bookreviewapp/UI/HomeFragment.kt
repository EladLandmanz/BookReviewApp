package com.example.bookreviewapp.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookreviewapp.Book
import com.example.bookreviewapp.databinding.FragmentHomeBinding
import com.example.bookreviewapp.UI.BookViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookViewModel by viewModels()
    private lateinit var adapter: BookAdapter
    private val bookList = mutableListOf<Book>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BookAdapter(bookList)
        binding.recommendedBooksRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recommendedBooksRecycler.adapter = adapter

        // Observe LiveData from ViewModel
        viewModel.books.observe(viewLifecycleOwner, Observer { books ->
            if (books != null) {
                bookList.clear()
                bookList.addAll(books)
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(requireContext(), "Failed to load books", Toast.LENGTH_SHORT).show()
            }
        })

        // Start fetching books
        viewModel.fetchBooks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
