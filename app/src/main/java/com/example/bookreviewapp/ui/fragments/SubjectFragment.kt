package com.example.bookreviewapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookreviewapp.databinding.FragmentSubjectBinding
import dagger.hilt.android.AndroidEntryPoint
import com.example.bookreviewapp.R
import com.example.bookreviewapp.viewmodel.BookViewModel
import com.example.bookreviewapp.adapters.BookAdapter
import com.example.bookreviewapp.adapters.CategoryAdapter
import com.example.bookreviewapp.data.models.Book
import com.example.bookreviewapp.utils.Loading
import com.example.bookreviewapp.utils.Success
import com.example.bookreviewapp.utils.Error
import com.example.bookreviewapp.viewmodel.SubjectsViewModel


@AndroidEntryPoint
class SubjectFragment : Fragment() {

    private var _binding: FragmentSubjectBinding? = null
    private val binding get() = _binding!!
    private lateinit var categoryAdapter :CategoryAdapter
    private val viewModel: SubjectsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerAndListeners()
        observeViewModelData()

    }


    private fun setupRecyclerAndListeners(){
        binding.subjectTitle.text = getString(R.string.subject_books_title)
        categoryAdapter = CategoryAdapter(mutableListOf(), object : BookAdapter.BooksListener {
            override fun onItemClicked(book: Book) {
                val bundle = Bundle().apply {
                    putString("bookId", book.id)
                }

                findNavController().navigate(R.id.bookDetailsFragment, bundle)
            }

            override fun onItemLongClicked(book: Book) {}
        })

        binding.subjectRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.subjectRecyclerView.adapter = categoryAdapter
//        viewModel.subjectBooks.observe(viewLifecycleOwner) { categories ->
//            for (category in categories) {
//                Log.d("SubjectDebug", "category: ${category.subject}")
//                for (book in category.books) {
//                    Log.d("SubjectDebug", " ${book.title} |  ${book.author} |  ${book.rating} |  ${book.imageUrl}")
//                }
//            }
//
//            categoryAdapter.updateCategories(categories)
//        }
    }


    private fun observeViewModelData(){
        viewModel.fetchBooksForSubjects(listOf("fantasy", "romance", "history", "mystery", "horror"))
        viewModel.subjectCategories.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                is Loading -> {
                    // Show a main progress bar for the whole subject view
                    binding.progressBar.visibility = View.VISIBLE
                    binding.errorTextView.visibility = View.GONE
                    // Hide RecyclerView content initially
                    binding.subjectRecyclerView.visibility = View.GONE
                }
                is Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorTextView.visibility = View.GONE
                    binding.subjectRecyclerView.visibility = View.VISIBLE // Show RecyclerView

                    val categories = resource.status.data ?: emptyList()
                    categoryAdapter.updateCategories(categories)  // Update the adapter with fresh data
                }
                is Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.errorTextView.text = resource.status.message ?: "Error"
                    Toast.makeText(requireContext(), "Error: ${resource.status.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
