package com.example.bookreviewapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookreviewapp.R
import com.example.bookreviewapp.viewmodel.BookViewModel
import com.example.bookreviewapp.ui.adapters.BookAdapter
import com.example.bookreviewapp.data.models.Book
import com.example.bookreviewapp.databinding.ResultFragmentBinding
import com.example.bookreviewapp.utils.Loading
import com.example.bookreviewapp.utils.Success
import com.example.bookreviewapp.viewmodel.ResultsViewModel

class ResultFragment : Fragment() {
    private var _binding: ResultFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ResultsViewModel by activityViewModels()
    private val args: ResultFragmentArgs by navArgs()
    private lateinit var adapter: BookAdapter
    private var hasShownNoResultsToast = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ResultFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerAndListeners()
        observeViewModelData()
    }

    private fun setupRecyclerAndListeners(){
        adapter = BookAdapter(mutableListOf(), object : BookAdapter.BooksListener {
            override fun onItemClicked(book: Book) {
                val bundle = Bundle().apply {
                    putString("bookId", book.id)
                }
                findNavController().navigate(R.id.bookDetailsFragment, bundle)
            }
            override fun onItemLongClicked(book: Book) {}
        })

        binding.recyclerResult.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerResult.adapter = adapter

        hasShownNoResultsToast = false
        val query = args.query
        binding.resultsTitle.text = getString(R.string.search_results_title, query)
    }

    private fun observeViewModelData() {
        viewModel.searchBooks(args.query)
        viewModel.resultBooks.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                is Loading -> {
                    // Show loading indicator
                    Log.d("ResultListDebug", "loading")
                    binding.progressBar.visibility = View.VISIBLE
                    binding.errorTextView.visibility = View.GONE
                    adapter.updateBooks(emptyList())
                }

                is Success -> {
                    Log.d("ResultListDebug", "Received results: ${resource.status.data?.size} books")
                    if (resource.status.data?.size != 0) {
                        Log.d("ResultListDebug", "list not empty")
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        hasShownNoResultsToast = false
    }
}

