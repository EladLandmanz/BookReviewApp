package com.example.bookreviewapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookreviewapp.databinding.FragmentMyReviewsBinding
import com.example.bookreviewapp.viewmodel.ReviewViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.bookreviewapp.adapters.ReviewBookAdapter

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

        val adapter = ReviewBookAdapter(emptyList())
        binding.reviewRecyclerView.adapter = adapter
        binding.reviewRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.booksWithReviews.observe(viewLifecycleOwner) { books ->
            adapter.updateData(books)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}