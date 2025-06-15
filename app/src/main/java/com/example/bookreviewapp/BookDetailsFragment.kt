package com.example.bookreviewapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.bookreviewapp.databinding.FragmentBookDetailsBinding
import com.example.bookreviewapp.entities.Book
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


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

        val bookId = arguments?.getString("bookId")
        Toast.makeText(requireContext()," book id: ${bookId}" , Toast.LENGTH_SHORT).show()
        if (bookId == null) {
            Toast.makeText(requireContext(), "Book ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.loadBook(bookId)

        binding.favoriteIcon.setOnClickListener {
            viewModel.book.value?.let {
                viewModel.toggleFavorite(it)
                val bounce = android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
                binding.favoriteIcon.startAnimation(bounce)
            }
        }

        binding.ratingBar.setOnRatingBarChangeListener { _, newRating, fromUser ->
            if (fromUser) {
                binding.youRated.text = ""
                viewModel.book.value?.let { book ->
                    book.rating = newRating
                    lifecycleScope.launch {
                        viewModel.updateBook(book)
                    }
                }
            }
        }

        viewModel.book.observe(viewLifecycleOwner) { book ->
            if (book != null) {
                binding.booktitle.text = book.title
                binding.bookAuthor.text = book.author
                binding.bookSummary.text = book.summary ?: "No summary available"
                binding.ratingBar.rating = book.rating

                val favoriteRes = if (book.isFavorite)
                    R.drawable.red_heart_favorite
                else
                    R.drawable.white_heart_favorite
                binding.favoriteIcon.setImageResource(favoriteRes)

                if (!book.imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(book.imageUrl)
                        .into(binding.bookImageCover)
                } else {
                    binding.bookImageCover.visibility = View.GONE
                }
            } else {
                Toast.makeText(requireContext(), "Book not found", Toast.LENGTH_SHORT).show()
            }
        }


    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null;
    }
}

