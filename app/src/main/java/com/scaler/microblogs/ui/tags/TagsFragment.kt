package com.scaler.microblogs.ui.tags

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.scaler.microblogs.adapters.TagsAdapter
import com.scaler.microblogs.databinding.FragmentTagsBinding
 import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TagsFragment : Fragment() {

    private val tagsViewModel: TagsViewModel by viewModels()
    private var _binding: FragmentTagsBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = TagsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTagsBinding.inflate(inflater, container, false)
        val tagAdapter = TagsAdapter(OnTagClick())

        tagsViewModel.getTags()

        binding.recyclerViewTags.apply {
            adapter = tagAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        tagsViewModel.tags.observe(viewLifecycleOwner) {
            it?.let {
                tagAdapter.submitList(it)
            }
        }

        return binding.root
    }

    inner class OnTagClick : TagsAdapter.OnItemTagClick {
        override fun onTagClick(tag: String) {
            val action =
                TagsFragmentDirections.actionNavTagsToTagsFeedFragment(tag)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}