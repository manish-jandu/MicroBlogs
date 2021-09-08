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
import com.scaler.microblogs.databinding.FragmentMyFeedBinding
import com.scaler.microblogs.utils.ArticleType
import com.scaler.microblogs.viewmodels.SharedGlobalMyFeedViewModel
import kotlinx.coroutines.flow.collect

class MyFeedFragment : Fragment(R.layout.fragment_my_feed) {
    private var _binding: FragmentMyFeedBinding? = null
    private val binding get() = _binding!!
    private val myFeedAdapter = ArticleAdapter(OnArticleClick(), ArticleType.ARTICLE)
    private val viewModel: SharedGlobalMyFeedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyFeedBinding.bind(view)

        binding.recyclerViewMyFeed.apply {
            adapter = myFeedAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.updateCurrentUserStatus()
        viewModel.isLoggedIn.observe(viewLifecycleOwner) {
            it?.let { isLoggedIn ->
                if (isLoggedIn) {
                    observeFeed()
                    setViewLoggedIn()
                } else {
                    setViewLoggedOut()
                }
            }
        }
    }

    private fun observeFeed() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getMyFeed().collect { data ->
                myFeedAdapter.submitData(data)
            }
        }
    }

    private fun setViewLoggedOut() {
        binding.apply {
            textViewError.text = "Login and try again"
            textViewError.visibility = View.VISIBLE
            imageViewError.visibility = View.VISIBLE
            progressBar.visibility = View.INVISIBLE
            recyclerViewMyFeed.visibility = View.INVISIBLE
        }
    }

    private fun setViewLoggedIn() {
        binding.apply {
            recyclerViewMyFeed.visibility = View.VISIBLE
            textViewError.visibility = View.INVISIBLE
            imageViewError.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE
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