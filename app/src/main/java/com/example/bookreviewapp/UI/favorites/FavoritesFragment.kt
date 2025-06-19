package com.example.bookreviewapp.UI.favorites

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookreviewapp.Book
import com.example.bookreviewapp.R
import com.example.bookreviewapp.UI.BookAdapter
import com.example.bookreviewapp.UI.BookViewModel
import com.example.bookreviewapp.databinding.FavoriteFragmentBinding
import com.example.bookreviewapp.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FavoritesFragment :Fragment() {

    // ViewBinding instance
    private var _binding: FavoriteFragmentBinding? = null
    private val binding get() = _binding!!

    // ViewModel instance
    private val viewModel: FavoritesViewModel by viewModels()

    // Adapter
    private lateinit var adapter: FavoriteBookAdapter

    private var pendingSearchQuery: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FavoriteFragmentBinding.inflate(inflater, container, false)
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
        binding.recyclerViewFavoriteBooks.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFavoriteBooks.adapter = adapter


    }
}