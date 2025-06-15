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
import com.example.bookreviewapp.R


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

        adapter = BookAdapter(mutableListOf(),object : BookAdapter.BooksListener {
            override fun onItemClicked(book: Book) {
                val bundle = Bundle().apply {
                    putString("bookId", book.id.toString())
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



        viewModel.fetchBooks()
        viewModel.books.observe(viewLifecycleOwner) { books ->
            Log.d("HomeFragment", "books size: ${books.size}")
            if (pendingSearchQuery != null) {
                if (books.isNotEmpty()){
                    val action = HomeFragmentDirections.actionHomeFragmentToResultFragment(pendingSearchQuery!!)
                    findNavController().navigate(action)
                } else{
                    Toast.makeText(requireContext(), getString(R.string.no_search_results), Toast.LENGTH_SHORT).show()
                    viewModel.fetchBooks()
                }
                pendingSearchQuery = null

            } else{
                adapter.updateBooks(books)
            }
            }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    pendingSearchQuery = query
                    viewModel.searchBooks(query)
//                    if (findNavController().currentDestination?.id == R.id.homeFragment) {
//                        val action = HomeFragmentDirections.actionHomeFragmentToResultFragment(query)
//                        findNavController().navigate(action)
                        //  }
                }

                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean=false
            })
//        viewModel.books.observe(viewLifecycleOwner) { books ->
//            pendingSearchQuery?.let { query ->
//                if (books.isNotEmpty()) {
//                    val action = HomeFragmentDirections.actionHomeFragmentToResultFragment(query)
//                    findNavController().navigate(action)
//                } else {
//                    Toast.makeText(requireContext(), getString(R.string.no_search_results), Toast.LENGTH_SHORT).show()
//                }
//                pendingSearchQuery = null
//            }
//        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    }
