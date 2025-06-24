package com.example.bookreviewapp.UI

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.bookreviewapp.Utils.Loading
import com.example.bookreviewapp.Utils.Success
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

        // Retrieve bookId from arguments
        val bookId = arguments?.getString("bookId")
        if (bookId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Book ID not found", Toast.LENGTH_LONG).show() // Use LONG for errors
            // Hide all content and show an error immediately if ID is missing
            binding.progressBar.visibility = View.GONE
            binding.errorTextView.visibility = View.VISIBLE
            binding.errorTextView.text = "Book ID is missing, cannot load details."
            // Assuming you have a ScrollView or Group for content. Set it to GONE.
            binding.detailsLinearLayout.visibility = View.GONE // Or binding.bookDetailsContentGroup.visibility = View.GONE
            return
        }

        // Initiate loading of the book details
        viewModel.loadBook(bookId)

        // Observe the LiveData<Resource<Book>> from the ViewModel
        viewModel.bookResource.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                is Loading -> {
                    // Show loading indicator
                    Log.d("DetailsDebug", "Loding resource")
                    binding.progressBar.visibility = View.VISIBLE
                    binding.errorTextView.visibility = View.GONE
                    binding.bookDetailsScrollView.visibility = View.GONE // Hide content while loading
                    binding.imageProgressBar.visibility = View.VISIBLE
                    binding.bookImageCover.visibility = View.VISIBLE // Ensure ImageView is visible for placeholder

                }
                is Success -> {
                    Log.d("DetailsDebug", "success resource")
                    // Hide loading, hide error, display book details
                    binding.progressBar.visibility = View.GONE
                    binding.errorTextView.visibility = View.GONE
                    binding.bookDetailsScrollView.visibility = View.VISIBLE // Show content

                    val book = resource.status.data
                    if (book != null) {
                        displayBookDetails(book)
                    } else {
                        binding.errorTextView.visibility = View.VISIBLE
                        binding.errorTextView.text = "Book data is null after successful fetch."
                        binding.detailsLinearLayout.visibility = View.GONE
                        Toast.makeText(requireContext(), "Book data is empty.", Toast.LENGTH_SHORT).show()
                    }
                }
                is com.example.bookreviewapp.Utils.Error<*> -> {
                    // Hide loading, show error message
                    binding.progressBar.visibility = View.GONE
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.errorTextView.text = resource.status.message ?: "An unknown error occurred."

                    // If error includes stale data, display it (offline support)
                    resource.status.data?.let { book ->
                        displayBookDetails(book)
                        binding.detailsLinearLayout.visibility = View.VISIBLE // Show stale content
                        Toast.makeText(requireContext(), "Offline: ${resource.status.message}", Toast.LENGTH_LONG).show()
                    } ?: run {
                        // No stale data available, hide content
                        binding.detailsLinearLayout.visibility = View.GONE
                        Toast.makeText(requireContext(), resource.status.message ?: "Failed to load book.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.favoriteIcon.setOnClickListener {
                viewModel.toggleFavorite()
                val bounce = android.view.animation.AnimationUtils.loadAnimation(requireContext(),
                    R.anim.bounce
                )
                binding.favoriteIcon.startAnimation(bounce)
        }

        binding.ratingBar.setOnRatingBarChangeListener { _, newRating, fromUser ->
            if (fromUser) {
                binding.youRated.text = ""
                viewModel.bookResource.value?.status?.data?.let { book ->
                    val updatedBook = book.copy(rating = newRating)
                    lifecycleScope.launch {
                        viewModel.updateBook(updatedBook)
                    }
                }
            }
        }
    }


    private fun displayBookDetails(book: Book) {
                binding.booktitle.text = book.title
                binding.bookSummary.text = book.summary ?: "No summary available"
                binding.ratingBar.rating = book.rating

                val favoriteRes = if (book.isFavorite)
                    R.drawable.red_heart_favorite
                else
                    R.drawable.white_heart_favorite
                binding.favoriteIcon.setImageResource(favoriteRes)



                if (!book.imageUrl.isNullOrEmpty()) {
                    binding.bookImageCover.visibility = View.VISIBLE // Ensure image view is visible
                    binding.imageProgressBar.visibility = View.VISIBLE // Show image progress bar BEFORE loading

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
                                // Optionally show a toast or change image to a more specific error drawable
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
                                return false // Let Glide display the resource
                            }
                        })
                        .into(binding.bookImageCover)
                } else {
                    binding.bookImageCover.visibility = View.GONE // Hide ImageView if no URL
                    binding.imageProgressBar.visibility = View.GONE // Hide progress bar too
                }
    }


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val bookId = arguments?.getString("bookId")
//        Toast.makeText(requireContext()," book id: ${bookId}" , Toast.LENGTH_SHORT).show()
//        if (bookId == null) {
//            Toast.makeText(requireContext(), "Book ID not found", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        viewModel.loadBook(bookId)
//
//        binding.favoriteIcon.setOnClickListener {
//            viewModel.book.value?.let {
//                viewModel.toggleFavorite(it)
//                val bounce = android.view.animation.AnimationUtils.loadAnimation(requireContext(),
//                    R.anim.bounce
//                )
//                binding.favoriteIcon.startAnimation(bounce)
//            }
//        }
//
//        binding.ratingBar.setOnRatingBarChangeListener { _, newRating, fromUser ->
//            if (fromUser) {
//                binding.youRated.text = ""
//                viewModel.book.value?.let { book ->
//                    book.rating = newRating
//                    lifecycleScope.launch {
//                        viewModel.updateBook(book)
//                    }
//                }
//            }
//        }
//
//        viewModel.bookResource.observe(viewLifecycleOwner) { book ->
//            if (book != null) {
//                binding.booktitle.text = book.title
//                binding.bookSummary.text = book.summary ?: "No summary available"
//                binding.ratingBar.rating = book.rating
//
//                val favoriteRes = if (book.isFavorite)
//                    R.drawable.red_heart_favorite
//                else
//                    R.drawable.white_heart_favorite
//                binding.favoriteIcon.setImageResource(favoriteRes)
//
//                if (!book.imageUrl.isNullOrEmpty()) {
//                    Glide.with(this)
//                        .load(book.imageUrl)
//                        .into(binding.bookImageCover)
//                } else {
//                    binding.bookImageCover.visibility = View.GONE
//                }
//            } else {
//                Toast.makeText(requireContext(), "Book not found", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//
//    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null;
    }
}

