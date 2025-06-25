package com.example.bookreviewapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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


@AndroidEntryPoint
class SubjectFragment : Fragment() {

    private var _binding: FragmentSubjectBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.subjectTitle.text = getString(R.string.subject_books_title)


        val categoryAdapter = CategoryAdapter(mutableListOf(), object : BookAdapter.BooksListener {
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
        viewModel.subjectBooks.observe(viewLifecycleOwner) { categories ->
            for (category in categories) {
                Log.d("SubjectDebug", "category: ${category.subject}")
                for (book in category.books) {
                    Log.d("SubjectDebug", " ${book.title} |  ${book.author} |  ${book.rating} |  ${book.imageUrl}")
                }
            }

            categoryAdapter.updateCategories(categories)
        }

        viewModel.fetchBooksGroupedBySubjects(
            listOf("fantasy", "romance", "history", "mystery", "horror")
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
