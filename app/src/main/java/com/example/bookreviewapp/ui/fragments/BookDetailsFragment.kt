package com.example.bookreviewapp.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.bookreviewapp.R
import com.example.bookreviewapp.databinding.FragmentBookDetailsBinding
import com.example.bookreviewapp.viewmodel.BookDetailsViewModel
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

        viewModel.book.observe(viewLifecycleOwner) { book ->
            if (book != null) {
                binding.booktitle.text = book.title
                binding.bookSummary.text = book.summary
                binding.ratingBar.rating = book.rating
                binding.youRated.text = if (book.rating > 0f){
                    getString(R.string.you_rated_with_value, book.rating)
                } else {
                    getString(R.string.you_rated)
                }

                binding.favoriteIcon.setImageResource(
                    if (book.isFavorite) R.drawable.red_heart_favorite
                    else R.drawable.white_heart_favorite
                )

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

        binding.favoriteIcon.setOnClickListener {
            viewModel.book.value?.let {
                viewModel.toggleFavorite(it)
                val bounce = android.view.animation.AnimationUtils.loadAnimation(requireContext(),
                    R.anim.bounce
                )
                binding.favoriteIcon.startAnimation(bounce)
            }
        }

        binding.ratingBar.setOnRatingBarChangeListener { _, newRating, fromUser ->
            if (fromUser) {
                viewModel.book.value?.let { book ->
                    book.rating = newRating
                    binding.youRated.text = getString(R.string.you_rated_with_value, newRating)
                    lifecycleScope.launch {
                        viewModel.updateBook(book)
                    }
                }
            }
        }

        binding.buttonAddReview.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_review, null)

            val alertDialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            val editReview = dialogView.findViewById<EditText>(R.id.editReview)
            val saveButton = dialogView.findViewById<Button>(R.id.btn_save)
            val cancelButton = dialogView.findViewById<Button>(R.id.btn_cancel)

            cancelButton.setOnClickListener {
                alertDialog.dismiss()
            }

            saveButton.setOnClickListener {
                val content = editReview.text.toString().trim()
                if (content.isNotEmpty()) {
                    if (viewModel.book.value != null) {
                        viewModel.book.value.let { book ->
                            viewModel.submitReview(book!!, content)
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.review_saved),
                                Toast.LENGTH_SHORT
                            ).show()
                            alertDialog.dismiss()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.empty_review), Toast.LENGTH_SHORT).show()
                }
            }
            alertDialog.show()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null;
    }
}

