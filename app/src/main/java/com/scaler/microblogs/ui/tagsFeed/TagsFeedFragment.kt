package com.scaler.microblogs.ui.tagsFeed

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.scaler.microblogs.R
import com.scaler.microblogs.adapters.PostAdapter
import com.scaler.microblogs.databinding.FragmentFeedBinding
import com.scaler.microblogs.databinding.FragmentTagsBinding
import com.scaler.microblogs.ui.feed.FeedViewModel
import com.scaler.microblogs.ui.tags.TagsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TagsFeedFragment : Fragment() {

    private val feedViewModel: TagsFeedViewModel by viewModels()
    private var _binding: FragmentTagsBinding? = null
    private val binding get() = _binding!!
    private val tagsFeedFragmentArgs: TagsFeedFragmentArgs by navArgs()

    companion object {
        fun newInstance() = TagsFeedFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTagsBinding.inflate(inflater, container, false)

        val tagToGetArticle = tagsFeedFragmentArgs.tag

        val adapter = PostAdapter()
        binding.recyclerViewTags.adapter = adapter
        binding.recyclerViewTags.layoutManager = LinearLayoutManager(requireContext())

        feedViewModel.getArticlesByTag(tagToGetArticle)

        feedViewModel.articleByTag.observe(viewLifecycleOwner) {
            it?.let {
                adapter.submitData(viewLifecycleOwner.lifecycle, it)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}