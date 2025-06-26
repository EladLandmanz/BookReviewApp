package com.example.bookreviewapp.ui.favorites

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookreviewapp.R
import com.example.bookreviewapp.utils.Loading
import com.example.bookreviewapp.utils.Success
import com.example.bookreviewapp.utils.Error
import com.example.bookreviewapp.data.models.Book
import com.example.bookreviewapp.databinding.FavoriteFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import com.example.bookreviewapp.utils.autoCleared


@AndroidEntryPoint
class FavoritesFragment :Fragment() {

    // ViewBinding instance
    private var binding: FavoriteFragmentBinding by autoCleared()

    // ViewModel instance
    private val viewModel: FavoritesViewModel by viewModels()
    private lateinit var adapter: FavoriteBookAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FavoriteFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("HomeFragment", "onViewCreated called")
        setupRecycler()
        viewModel.fetchFavoriteBooks()
        observeViewModelData()
    }

    private fun setupRecycler() {
        // Setup RecyclerView with adapter and layout manager
        adapter = FavoriteBookAdapter(object : FavoriteBookAdapter.BookListener {
            override fun onItemClick(book: Book) {
                val bundle = Bundle().apply {
                    putString("bookId", book.id)
                }
                findNavController().navigate(R.id.bookDetailsFragment, bundle)
            }

            override fun onItemLongClick(book: Book) {
                TODO("Not yet implemented")
            }
        })
        binding.recyclerViewFavoriteBooks.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFavoriteBooks.adapter = adapter

    }

    private fun observeViewModelData() {
        viewModel.favoriteBooks.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                is Loading -> {
                    // Show loading indicator
                    Log.d("FavoriteListDebug", "loading")
                    binding.progressBar.visibility = View.VISIBLE
                    binding.errorTextView.visibility = View.GONE
                    binding.emptyListMessage.visibility = View.GONE
                }

                is Success -> {
                    Log.d("FavoriteListDebug", "Received favorites: ${resource.status.data?.size} books")
                    // Hide loading indicator, display data
                    binding.progressBar.visibility = View.GONE
                    binding.errorTextView.visibility = View.GONE
                    val books = resource.status.data ?: emptyList()
                    adapter.submitList(books)
                    binding.emptyListMessage.visibility =
                        if (books.isEmpty()) View.VISIBLE else View.GONE
                }

                is Error -> {
                    // Hide loading, show error message
                    binding.progressBar.visibility = View.GONE
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.errorTextView.text = resource.status.message
                    Toast.makeText(context, resource.status.message, Toast.LENGTH_SHORT).show()
                }

            }
        }
    }
}