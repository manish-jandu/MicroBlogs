package com.scaler.microblogs.ui.feed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.scaler.microblogs.R
import com.scaler.microblogs.adapters.ArticleAdapter
import com.scaler.microblogs.databinding.FragmentFeedBinding
import com.scaler.microblogs.utils.ArticleType
import com.scaler.microblogs.utils.InternetConnectivity.ConnectivityManager
import com.scaler.microblogs.viewmodels.FeedViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment(R.layout.fragment_feed) {

    private val feedViewModel: FeedViewModel by viewModels()
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var connectivityManager:ConnectivityManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedBinding.bind(view)

        observeInternet()
        observeCurrentUserStatus()
        setupFloatingButton()
    }

    private fun observeInternet() {
        connectivityManager.isNetworkAvailable.observe(viewLifecycleOwner) { it ->
            it?.let {
                feedViewModel.isInternetAvailable = it
                setupView()
            }
        }
    }


    private fun observeCurrentUserStatus() {
        feedViewModel.updateCurrentUserStatus()
        feedViewModel.isLoggedIn.observe(viewLifecycleOwner){
            it?.let {
                setupTabsLayout(it)
            }
        }
    }

    private fun setupFloatingButton() {
        binding.floatingButtonAddArticle.setOnClickListener {
            val action = FeedFragmentDirections.actionNavFeedToAddEditArticleFragment()
            findNavController().navigate(action)
        }
    }

    private fun setupView() {
        //Todo:set view that toggle when there is no internet
    }

    private fun setupTabsLayout(isLoggedIn:Boolean){
        //Todo:set tabs layout with viewpager
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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}