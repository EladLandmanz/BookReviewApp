package com.example.bookreviewapp.UI

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
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
    private val viewModel: BookViewModel by activityViewModels()

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
        Log.d("HomeFragment", "onViewCreated called")
        // Setup RecyclerView with adapter and layout manager
        adapter = BookAdapter(mutableListOf<Book>())
        binding.bookRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.bookRecyclerView.adapter = adapter

        viewModel.fetchBooks()
        viewModel.books.observe(viewLifecycleOwner) { books ->
            Log.d("HomeFragment", "books size: ${books.size}")
            adapter.updateBooks(books)
            }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    val action = HomeFragmentDirections.actionHomeFragmentToResultFragment(query)
                    findNavController().navigate(action)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean = false
            })
        }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    }
