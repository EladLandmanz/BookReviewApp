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
import com.example.bookreviewapp.ui.adapters.BookAdapter
import com.example.bookreviewapp.utils.Loading
import com.example.bookreviewapp.utils.Success


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
        // Setup RecyclerView with adapter and layout manager and all the buttons
        setupRecyclerAndListeners()
        //setup the searchView
        setupSearchView()
        //start observing the data
        observeViewModelData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //observe the trending books live data and update to match its state
    private fun observeViewModelData() {
        viewModel.fetchTrendingBooks()
        viewModel.trendingBooks.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                is Loading -> {
                    // Show loading indicator
                    Log.d("trendingListDebug", "loading")
                    binding.progressBar.visibility = View.VISIBLE
                    binding.errorTextView.visibility = View.GONE
                }

                is Success -> {
                    Log.d("trendingListDebug", "Received favorites: ${resource.status.data?.size} books")
                    if (resource.status.data?.size != 0){
                        Log.d("trendingListDebug", "list not empty")
                        // Hide loading indicator, display data
                        binding.progressBar.visibility = View.GONE
                        binding.errorTextView.visibility = View.GONE
                        val books = resource.status.data ?: emptyList()
                        adapter.updateBooks(books)
                    }
                }


                is com.example.bookreviewapp.utils.Error<*> -> {
                    // Hide loading, show error message
                    binding.progressBar.visibility = View.GONE
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.errorTextView.text = resource.status.message
                    Toast.makeText(context, resource.status.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    //setup the search view
    private fun setupSearchView() {

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    val action = HomeFragmentDirections.actionHomeFragmentToResultFragment(query)
                    findNavController().navigate(action)
                    binding.searchView.clearFocus()
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }

    private fun setupRecyclerAndListeners(){

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
        binding.favoritesButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_favoritesFragment)
        }
        binding.myReviewsButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_myReviewsFragment)
        }
    }

}

//    adapter = BookAdapter(mutableListOf(), object : BookAdapter.BooksListener {
//        override fun onItemClicked(book: Book) {
//            val bundle = Bundle().apply {
//                putString("bookId", book.id)
//            }
//
//            findNavController().navigate(R.id.bookDetailsFragment, bundle)
//        }
//
//        override fun onItemLongClicked(book: Book) {
//            // Toast.makeText(requireContext(),"${viewModel.getItem(position)}",Toast.LENGTH_SHORT).show()
//        }
//    })
//    binding.bookRecyclerView.layoutManager = LinearLayoutManager(requireContext())
//    binding.bookRecyclerView.adapter = adapter
//    binding.searchView.queryHint = getString(R.string.search_hint)
//    binding.btnBrowseBySubject.setOnClickListener {
//        findNavController().navigate(R.id.action_homeFragment_to_subjectFragment)
//    }
//    binding.favoritesButton.setOnClickListener {
//        findNavController().navigate(R.id.action_homeFragment_to_favoritesFragment)
//    }
//
//    viewModel.fetchBooks()
//    viewModel.books.observe(viewLifecycleOwner) { books ->
//        Log.d("HomeFragment", "books size: ${books.size}")
//        if (pendingSearchQuery != null) {
//            if (books.isNotEmpty()) {
//                val action =
//                    HomeFragmentDirections.actionHomeFragmentToResultFragment(pendingSearchQuery!!)
//                findNavController().navigate(action)
//            } else {
//                Toast.makeText(
//                    requireContext(),
//                    getString(R.string.no_search_results),
//                    Toast.LENGTH_SHORT
//                ).show()
//                viewModel.fetchBooks()
//            }
//            pendingSearchQuery = null
//
//        } else {
//            adapter.updateBooks(books)
//        }
//    }

