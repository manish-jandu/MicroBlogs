package com.scaler.microblogs.ui.feed

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.scaler.microblogs.R
import com.scaler.microblogs.adapters.PostAdapter
import com.scaler.microblogs.databinding.FragmentFeedBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private val feedViewModel: FeedViewModel by viewModels()
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = FeedFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val adapter = PostAdapter()
        binding.recyclerViewFeed.adapter = adapter
        binding.recyclerViewFeed.layoutManager = LinearLayoutManager(requireContext())

        feedViewModel.articles.observe(viewLifecycleOwner) {
            adapter.submitData(viewLifecycleOwner.lifecycle, it)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}