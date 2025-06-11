package com.example.bookreviewapp.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookreviewapp.Book
import com.example.bookreviewapp.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    // ViewBinding instance
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // ViewModel instance
    private val viewModel: BookViewModel by viewModels()

    // Adapter and list of books
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

        // Setup RecyclerView with adapter and layout manager
        adapter = BookAdapter(bookList)
        binding.bookRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.bookRecyclerView.adapter = adapter

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

        // Start fetching books from ViewModel
        viewModel.fetchBooks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
