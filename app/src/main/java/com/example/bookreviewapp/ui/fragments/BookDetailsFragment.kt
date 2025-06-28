package com.example.bookreviewapp.ui.fragments

import android.app.AlertDialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
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
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.target.Target
import com.example.bookreviewapp.R
import com.example.bookreviewapp.ui.BookDetailsViewModel
import com.example.bookreviewapp.utils.Loading
import com.example.bookreviewapp.utils.Success
import com.example.bookreviewapp.utils.Error
import com.example.bookreviewapp.data.models.Book
import com.example.bookreviewapp.databinding.FragmentBookDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class BookDetailsFragment : Fragment(R.layout.fragment_book_details) {

    private var _binding: FragmentBookDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookDetailsViewModel by viewModels()

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
        if (bookId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Book ID not found", Toast.LENGTH_LONG)
                .show()
            binding.progressBar.visibility = View.GONE
            binding.errorTextView.visibility = View.VISIBLE
            binding.errorTextView.text = "Book ID is missing, cannot load details."
            binding.detailsLinearLayout.visibility =
                View.GONE
            return
        }

        viewModel.loadBook(bookId)

        viewModel.bookResource.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                is Loading -> {
                    Log.d("DetailsDebug", "Loding resource")
                    binding.progressBar.visibility = View.VISIBLE
                    binding.errorTextView.visibility = View.GONE
                    binding.bookDetailsScrollView.visibility =
                        View.GONE
                    binding.imageProgressBar.visibility = View.VISIBLE
                    binding.bookImageCover.visibility =
                        View.VISIBLE

                }

                is Success -> {
                    Log.d("DetailsDebug", "success resource")
                    binding.progressBar.visibility = View.GONE
                    binding.errorTextView.visibility = View.GONE
                    binding.bookDetailsScrollView.visibility = View.VISIBLE

                    val book = resource.status.data
                    if (book != null) {
                        displayBookDetails(book)
                    } else {
                        binding.errorTextView.visibility = View.VISIBLE
                        binding.errorTextView.text = "Book data is null after successful fetch."
                        binding.detailsLinearLayout.visibility = View.GONE
                        Toast.makeText(requireContext(), "Book data is empty.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                is Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.errorTextView.text =
                        resource.status.message
                }
            }
        }

        val currentBook = viewModel.bookResource.value?.status?.data

        binding.favoriteIcon.setOnClickListener {
            viewModel.toggleFavorite()
            val bounce = android.view.animation.AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.bounce
            )
            binding.favoriteIcon.startAnimation(bounce)
        }

        binding.ratingBar.setOnRatingBarChangeListener { _, newRating, fromUser ->
            if (fromUser) {
                viewModel.updateRating(newRating)
                binding.youRated.text = getString(R.string.you_rated_with_value, newRating)
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
                        viewModel.submitReview(content)
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.review_saved),
                            Toast.LENGTH_SHORT
                        ).show()
                        alertDialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.empty_review), Toast.LENGTH_SHORT).show()
                }
            }
            alertDialog.show()
        }
    }

    private fun displayBookDetails(book: Book) {
        binding.booktitle.text = book.title
        binding.bookSummary.text = book.summary ?: "No summary available"
        binding.ratingBar.rating = book.rating
        binding.youRated.text = if (book.rating > 0f){
            getString(R.string.you_rated_with_value, book.rating)
        }
        else{
            getString(R.string.you_rated)
        }

        val favoriteRes = if (book.isFavorite)
            R.drawable.red_heart_favorite
        else
            R.drawable.white_heart_favorite
        binding.favoriteIcon.setImageResource(favoriteRes)

        if (!book.imageUrl.isNullOrEmpty()) {
            binding.bookImageCover.visibility = View.VISIBLE
            binding.imageProgressBar.visibility = View.VISIBLE

            Glide.with(this)
                .load(book.imageUrl)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.imageProgressBar.visibility = View.GONE
                        Toast.makeText(context, "Failed to load image.", Toast.LENGTH_SHORT).show()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.imageProgressBar.visibility = View.GONE
                        return false
                    }
                })
                .into(binding.bookImageCover)
        } else {
            binding.bookImageCover.visibility = View.GONE
            binding.imageProgressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null;
    }
}

