package com.scaler.microblogs.ui.feed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.scaler.microblogs.R
import com.scaler.microblogs.adapters.ArticleAdapter
import com.scaler.microblogs.databinding.FragmentGlobalFeedBinding
import com.scaler.microblogs.utils.ArticleType
import com.scaler.microblogs.viewmodels.SharedGlobalMyFeedViewModel
import kotlinx.coroutines.flow.collect

class GlobalFeedFragment : Fragment(R.layout.fragment_global_feed) {
    private var _binding: FragmentGlobalFeedBinding? = null
    private val binding get() = _binding!!
    private val globalFeedAdapter = ArticleAdapter(OnArticleClick(), ArticleType.ARTICLE)
    private val viewModel: SharedGlobalMyFeedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGlobalFeedBinding.bind(view)

        binding.recyclerViewGlobalFeed.apply {
            adapter = globalFeedAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getGlobalFeed().collect { data ->
                globalFeedAdapter.submitData(data)
            }
        }
    }

    inner class OnArticleClick() : ArticleAdapter.OnArticleClick {
        override fun onItemClick(slug: String, articleType: ArticleType) {
            val action =
                FeedFragmentDirections.actionNavFeedToArticleFragment(articleType, slug)
            findNavController().navigate(action)
        }

        override fun onProfileClick(userName: String) {
            val action = FeedFragmentDirections.actionNavFeedToProfileFragment(userName)
            findNavController().navigate(action)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}