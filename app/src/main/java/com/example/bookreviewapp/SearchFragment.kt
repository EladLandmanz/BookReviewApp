package com.example.bookreviewapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookreviewapp.UI.BookAdapter
import com.example.bookreviewapp.UI.BookViewModel
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import com.example.bookreviewapp.databinding.SearchFragmentBinding

class SearchFragment :Fragment() {
    private var _binding : SearchFragmentBinding? = null

    private val binding get() = _binding!!

    private val viewModel: BookViewModel by activityViewModels()
    private lateinit var adapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SearchFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchView.queryHint = getString(R.string.search_hint)
        binding.searchView.setIconifiedByDefault(false)
        adapter = BookAdapter(mutableListOf(),object : BookAdapter.BooksListener {
            override fun onItemClicked(position: Int) {

                findNavController().navigate(R.id.bookDetailsFragment)
            }

            override fun onItemLongClicked(position: Int) {
                // Toast.makeText(requireContext(),"${viewModel.getItem(position)}",Toast.LENGTH_SHORT).show()
            }
        })
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.searchRecyclerView.adapter = adapter
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {

                    viewModel.searchBooks(query)
                }
                return true
        }
            override fun onQueryTextChange(newText: String?): Boolean = false
        })
        viewModel.books.observe(viewLifecycleOwner) { books ->
            adapter.updateBooks(books)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}