package com.scaler.microblogs.ui.favouritearticles

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.LinearLayoutManager
import com.scaler.microblogs.R
import com.scaler.microblogs.adapters.ArticleAdapter
import com.scaler.microblogs.databinding.FragmentFavouriteArticlesBinding
import com.scaler.microblogs.utils.ArticleType
import com.scaler.microblogs.viewmodels.SharedFavUserArticlesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class FavouriteArticlesFragment(private val userName: String, articleType: ArticleType,OnArticleClick: ArticleAdapter.OnArticleClick) :
    Fragment(R.layout.fragment_favourite_articles) {
    private var _binding: FragmentFavouriteArticlesBinding? = null
    private val binding get() = _binding!!
    private val accountViewModel: SharedFavUserArticlesViewModel by viewModels()
    private val favouriteArticlesAdapter = ArticleAdapter(OnArticleClick, articleType)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavouriteArticlesBinding.bind(view)

        binding.recyclerViewUserFavouritesFeed.apply {
            adapter = favouriteArticlesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        getFavouriteArticles(userName)

    }

    private fun getFavouriteArticles(userName: String) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            accountViewModel.getUserFavouriteArticle(userName).collectLatest { data ->
                favouriteArticlesAdapter.submitData(viewLifecycleOwner.lifecycle, data)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

fun NavController.safeNavigate(direction: NavDirections) {
    currentDestination?.getAction(direction.actionId)?.run { navigate(direction) }
}