package com.scaler.microblogs.ui.userarticles

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.scaler.microblogs.R
import com.scaler.microblogs.adapters.ArticleAdapter
import com.scaler.microblogs.databinding.FragmentUserArticlesBinding
import com.scaler.microblogs.ui.account.AccountFragmentDirections
import com.scaler.microblogs.utils.ArticleType
import com.scaler.microblogs.viewmodels.AccountViewModel
import kotlinx.coroutines.flow.collectLatest

class UserArticlesFragment : Fragment(R.layout.fragment_user_articles) {
    private var _binding: FragmentUserArticlesBinding? = null
    private val binding get() = _binding!!
    private val accountViewModel: AccountViewModel by activityViewModels()
    private val userArticleAdapter =
        ArticleAdapter(OnArticleClick(), ArticleType.USER_CREATED_ARTICLE)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserArticlesBinding.bind(view)

        binding.recyclerViewUserFeed.apply {
            adapter = userArticleAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        accountViewModel.userName.observe(viewLifecycleOwner) {
            it?.let {
                getUserArticles(it)
            }
        }
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
            val action = AccountFragmentDirections.actionNavAccountToArticleFragment(articleType, slug)
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