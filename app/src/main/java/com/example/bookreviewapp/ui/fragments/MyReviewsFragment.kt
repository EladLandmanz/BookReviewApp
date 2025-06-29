package com.example.bookreviewapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookreviewapp.R
import com.example.bookreviewapp.ui.adapters.BookAdapter
import com.example.bookreviewapp.databinding.FragmentMyReviewsBinding
import com.example.bookreviewapp.viewmodel.ReviewViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.bookreviewapp.ui.adapters.ReviewBookAdapter
import com.example.bookreviewapp.data.models.Book

@AndroidEntryPoint
class MyReviewsFragment : Fragment() {

    private var _binding: FragmentMyReviewsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReviewViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ReviewBookAdapter(mutableListOf(), object : ReviewBookAdapter.ReviewBooksListener {
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
        binding.reviewRecyclerView.adapter = adapter
        binding.reviewRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewModel.fetchBooksWithReviews()
        viewModel.booksWithReviews.observe(viewLifecycleOwner) { books ->
            adapter.updateData(books)
        }

        ItemTouchHelper (object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return makeMovementFlags(0, ItemTouchHelper.RIGHT)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int){
                val position = viewHolder.adapterPosition
                val book = adapter.getBookAt(position)
                book.review = null
                viewModel.submitReview(book, "")
                Toast.makeText(requireContext(), getString(R.string.review_deleted), Toast.LENGTH_SHORT).show()
            }

        }).attachToRecyclerView(binding.reviewRecyclerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}