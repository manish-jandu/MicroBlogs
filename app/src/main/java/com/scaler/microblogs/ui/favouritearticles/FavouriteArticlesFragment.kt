package com.scaler.microblogs.ui.favouritearticles

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.scaler.microblogs.R
import com.scaler.microblogs.adapters.ArticleAdapter
import com.scaler.microblogs.databinding.FragmentFavouriteArticlesBinding
import com.scaler.microblogs.utils.ArticleType
import com.scaler.microblogs.viewmodels.AccountViewModel
import kotlinx.coroutines.flow.collectLatest

class FavouriteArticlesFragment : Fragment(R.layout.fragment_favourite_articles) {
    private var _binding: FragmentFavouriteArticlesBinding? = null
    private val binding get() = _binding!!
    private val accountViewModel: AccountViewModel by activityViewModels()
    private val favouriteArticlesAdapter = ArticleAdapter(OnArticleClick(), ArticleType.ARTICLE)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavouriteArticlesBinding.bind(view)

        binding.recyclerViewUserFavouritesFeed.apply {
            adapter = favouriteArticlesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        accountViewModel.userName.observe(viewLifecycleOwner) {
            it?.let {
                getFavouriteArticles(it)
            }
        }

    }

    private fun getFavouriteArticles(userName: String) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            accountViewModel.getUserFavouriteArticle(userName).collectLatest { data ->
                favouriteArticlesAdapter.submitData(viewLifecycleOwner.lifecycle, data)
            }
        }
    }

    inner class OnArticleClick() : ArticleAdapter.OnArticleClick {
        override fun onItemClick(slug: String, articleType: ArticleType) {

        }

        override fun onProfileClick(userName: String) {

        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
