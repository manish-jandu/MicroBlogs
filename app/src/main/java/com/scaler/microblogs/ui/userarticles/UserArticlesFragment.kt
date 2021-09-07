package com.scaler.microblogs.ui.userarticles

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.scaler.microblogs.R
import com.scaler.microblogs.adapters.ArticleAdapter
import com.scaler.microblogs.databinding.FragmentUserArticlesBinding
import com.scaler.microblogs.ui.account.AccountFragmentDirections
import com.scaler.microblogs.utils.ArticleType
import com.scaler.microblogs.viewmodels.SharedFavUserArticlesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class UserArticlesFragment(private val userName: String, articleType: ArticleType) :
    Fragment(R.layout.fragment_user_articles) {
    private var _binding: FragmentUserArticlesBinding? = null
    private val binding get() = _binding!!
    private val accountViewModel: SharedFavUserArticlesViewModel by viewModels()
    private val userArticleAdapter =
        ArticleAdapter(OnArticleClick(), articleType)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserArticlesBinding.bind(view)

        binding.recyclerViewUserFeed.apply {
            adapter = userArticleAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        getUserArticles(userName)
    }

    private fun getUserArticles(userName: String) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            accountViewModel.getUserArticles(userName).collectLatest { data ->
                userArticleAdapter.submitData(viewLifecycleOwner.lifecycle, data)
            }
        }
    }

    inner class OnArticleClick() : ArticleAdapter.OnArticleClick {
        override fun onItemClick(slug: String, articleType: ArticleType) {
            val action =
                AccountFragmentDirections.actionNavAccountToArticleFragment(articleType, slug)
            findNavController().navigate(action)
        }

        override fun onProfileClick(userName: String) {
            val action = AccountFragmentDirections.actionNavAccountToProfileFragment(userName)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}