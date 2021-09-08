package com.scaler.microblogs.ui.tagsFeed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.scaler.microblogs.R
import com.scaler.microblogs.adapters.ArticleAdapter
import com.scaler.microblogs.databinding.FragmentTagsFeedBinding
import com.scaler.microblogs.utils.ArticleType
import com.scaler.microblogs.utils.InternetConnectivity.ConnectivityManager
import com.scaler.microblogs.viewmodels.TagsFeedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class TagsFeedFragment : Fragment(R.layout.fragment_tags_feed) {
    private var _binding: FragmentTagsFeedBinding? = null
    private val binding get() = _binding!!
    private val feedViewModel: TagsFeedViewModel by viewModels()
    private val tagsFeedFragmentArgs: TagsFeedFragmentArgs by navArgs()
    private val articleAdapter = ArticleAdapter(OnArticleClick(), ArticleType.ARTICLE)

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding =FragmentTagsFeedBinding.bind(view)
        val tagToGetArticle = tagsFeedFragmentArgs.tag

        binding.recyclerViewTagsFeed.apply {
            adapter = articleAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        observeInternet()
        observeArticles(tagToGetArticle)
    }

    private fun observeInternet() {
        connectivityManager.isNetworkAvailable.observe(viewLifecycleOwner) {
            it?.let { isInternetAvailable ->
                if(isInternetAvailable){
                    setViewTagsFeed()
                }else{
                    setViewError()
                }
            }
        }
    }

    private fun observeArticles(tagToGetArticle: String) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            feedViewModel.getArticlesByTag(tagToGetArticle).collect { data ->
                articleAdapter.submitData(data)
            }
        }
    }

    private fun setViewError(message: String = "No Internet Connection") {
        binding.apply {
            textViewError.text = message
            textViewError.visibility = View.VISIBLE
            imageViewError.visibility = View.VISIBLE
            recyclerViewTagsFeed.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE
        }
    }

    private fun setViewTagsFeed() {
        binding.apply {
            recyclerViewTagsFeed.visibility = View.VISIBLE
            progressBar.visibility = View.INVISIBLE
            imageViewError.visibility = View.INVISIBLE
            textViewError.visibility = View.INVISIBLE
        }
    }

    inner class OnArticleClick() : ArticleAdapter.OnArticleClick {
        override fun onItemClick(slug: String, articleType: ArticleType) {
            val action =
                TagsFeedFragmentDirections.actionTagsFeedFragmentToArticleFragment(
                    articleType,
                    slug
                )
            findNavController().navigate(action)
        }

        override fun onProfileClick(userName: String) {
            val action =
                TagsFeedFragmentDirections.actionTagsFeedFragmentToProfileFragment(userName)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}