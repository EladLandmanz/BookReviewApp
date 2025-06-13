package com.example.bookreviewapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bookreviewapp.databinding.SearchFragmentBinding

class SearchFragment :Fragment() {
    private var _binding : SearchFragmentBinding? = null

    private val binding get() = _binding!!

    private var imageUri : Uri? = null

    val pickItemLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()){
            binding.imageView.setImageURI(it)
            requireActivity().contentResolver.takePersistableUriPermission(it!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            imageUri = it
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SearchFragmentBinding.inflate(inflater,container,false)

        binding.floatingActionButton.setOnClickListener{
            val book = Book(1, binding.bookTitleEt.text.toString(), binding.bookAuthorEt.text.toString(), 1.5f, "good the book", imageUri.toString())
            BooksManager.add(book)
            findNavController().navigate(R.id.action_searchFragment_to_resultFragment)
        }
        binding.imageBtn.setOnClickListener{
            pickItemLauncher.launch(arrayOf("image/*"))
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}