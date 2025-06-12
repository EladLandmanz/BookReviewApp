package com.example.bookreviewapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.bookreviewapp.databinding.FragmentBookDetailsBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BookDetailsFragment : Fragment(R.layout.fragment_book_details) {

    private var _binding: FragmentBookDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel : BookDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // get the book details
        val args = BookDetailsFragmentArgs.fromBundle(requireArguments())
        val bookId = args.bookId.toString()
        viewModel.loadBook(bookId)
        viewModel.book.observe(viewLifecycleOwner) { book ->
            book?.let {
                binding.booktitle.text = it.title
                binding.textSummary.text = it.summary ?: "No summary available"
                binding.ratingBar.rating = it.rating
                if (!it.imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(it.imageUrl)
                        .into(binding.bookImageCover)
                } else {
                    binding.bookImageCover.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null;
    }

}