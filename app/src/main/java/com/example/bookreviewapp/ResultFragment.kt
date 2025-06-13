package com.example.bookreviewapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookreviewapp.UI.BookAdapter
import com.example.bookreviewapp.databinding.ResultFragmentBinding
import com.example.bookreviewapp.databinding.SearchFragmentBinding

class ResultFragment : Fragment() {
    private var _binding : ResultFragmentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ResultFragmentBinding.inflate(inflater,container,false)

        binding.finishBtn.setOnClickListener{

            findNavController().navigate(R.id.action_resultFragment_to_searchFragment)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerSearch.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSearch.adapter = BookAdapter(BooksManager.resultBooks, object : BookAdapter.BookListener{
            override fun onBookClicked(index: Int) {
                Toast.makeText(requireContext(), "${BooksManager.resultBooks[index]}", Toast.LENGTH_SHORT).show()
            }

            override fun onBookLongClick(index: Int) {
                TODO("Not yet implemented")
            }
        })
        ItemTouchHelper(object : ItemTouchHelper.Callback(){
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int = makeFlag(ACTION_STATE_SWIPE, LEFT or RIGHT)

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                BooksManager.remove(viewHolder.adapterPosition)
                binding.recyclerSearch.adapter!!.notifyItemRemoved(viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(binding.recyclerSearch)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}