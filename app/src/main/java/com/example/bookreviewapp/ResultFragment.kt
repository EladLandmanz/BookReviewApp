package com.example.bookreviewapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookreviewapp.UI.BookAdapter
import com.example.bookreviewapp.UI.BookViewModel
import com.example.bookreviewapp.databinding.ResultFragmentBinding
import com.example.bookreviewapp.databinding.SearchFragmentBinding

class ResultFragment : Fragment() {
    private var _binding: ResultFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookViewModel by activityViewModels()
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
        adapter = BookAdapter(mutableListOf(),object : BookAdapter.BooksListener {
            override fun onItemClicked(book: Book) {

                findNavController().navigate(R.id.bookDetailsFragment)
            }

            override fun onItemLongClicked(book: Book) {
                // Toast.makeText(requireContext(),"${viewModel.getItem(position)}",Toast.LENGTH_SHORT).show()
            }
            })

        binding.recyclerResult.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerResult.adapter = adapter
        viewModel.searchBooks(args.query) // run the search with the query
        hasShownNoResultsToast = false
        val query = args.query
        binding.resultsTitle.text = getString(R.string.search_results_title, query)


        viewModel.books.observe(viewLifecycleOwner) { books ->
            if (books.isEmpty() && !hasShownNoResultsToast) {
                hasShownNoResultsToast = true
                Toast.makeText(
                    requireContext(),
                    getString(R.string.no_search_results),
                    Toast.LENGTH_SHORT
                ).show()
            }
            adapter.updateBooks(books)
        }
    }

            override fun onDestroyView() {
                super.onDestroyView()
                _binding = null
                hasShownNoResultsToast = false
            }
        }
