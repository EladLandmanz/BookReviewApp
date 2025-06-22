package com.example.bookreviewapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookreviewapp.data.models.Book
import com.example.bookreviewapp.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import com.example.bookreviewapp.R
import com.example.bookreviewapp.viewmodel.BookViewModel
import com.example.bookreviewapp.adapters.BookAdapter



@AndroidEntryPoint
class HomeFragment : Fragment() {
    // ViewBinding instance
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // ViewModel instance
    private val viewModel: BookViewModel by activityViewModels()

    // Adapter
    private lateinit var adapter: BookAdapter

    private var pendingSearchQuery: String? = null


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

        adapter = BookAdapter(mutableListOf(), object : BookAdapter.BooksListener {
            override fun onItemClicked(book: Book) {
                val bundle = Bundle().apply {
                    putString("bookId", book.id)
                }

                findNavController().navigate(R.id.bookDetailsFragment, bundle)
            }

            override fun onItemLongClicked(book: Book) {
                // Toast.makeText(requireContext(),"${viewModel.getItem(position)}",Toast.LENGTH_SHORT).show()
            }
        })
        binding.bookRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.bookRecyclerView.adapter = adapter
        binding.searchView.queryHint = getString(R.string.search_hint)
        binding.btnBrowseBySubject.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_subjectFragment)
        }

        viewModel.fetchBooks()
        viewModel.books.observe(viewLifecycleOwner) { books ->
            Log.d("HomeFragment", "books size: ${books.size}")
            if (pendingSearchQuery != null) {
                if (books.isNotEmpty()) {
                    val action =
                        HomeFragmentDirections.actionHomeFragmentToResultFragment(pendingSearchQuery!!)
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.no_search_results),
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.fetchBooks()
                }
                pendingSearchQuery = null

            } else {
                adapter.updateBooks(books)
            }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    pendingSearchQuery = query
                    viewModel.searchBooks(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        binding.myReviewsButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_myReviewsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
