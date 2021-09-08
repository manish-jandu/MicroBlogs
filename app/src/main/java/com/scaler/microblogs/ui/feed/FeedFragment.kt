package com.scaler.microblogs.ui.feed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.scaler.microblogs.R
import com.scaler.microblogs.adapters.viewpager.AccountViewPagerAdapter
import com.scaler.microblogs.databinding.FragmentFeedBinding
import com.scaler.microblogs.utils.InternetConnectivity.ConnectivityManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment(R.layout.fragment_feed) {
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private var isInternetAvailable: Boolean? = null

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedBinding.bind(view)

        observeInternet()
        setupTabsLayout()
        setupFloatingButton()
    }

    private fun observeInternet() {
        connectivityManager.isNetworkAvailable.observe(viewLifecycleOwner) { it ->
            it?.let {
                isInternetAvailable = it
                setupView()
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
        if (isInternetAvailable != null || isInternetAvailable == true) {
            setViewFeed()
        } else {
            setViewError()
        }
    }

    private fun setViewFeed() {
        binding.apply {
            groupShowFeed.visibility = View.VISIBLE
            groupError.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE
        }
    }

    private fun setViewError(message: String = "No Internet Available.") {
        binding.apply {
            textViewError.text = message
            groupError.visibility = View.VISIBLE
            groupShowFeed.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE
        }
    }

    private fun setupTabsLayout() {
        binding.viewPagerAccount.adapter = getPagerAdapter()

        val titles = arrayListOf("Global Feed", "Your Feed")
        TabLayoutMediator(
            binding.tabsLayout,
            binding.viewPagerAccount
        ) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    private fun getPagerAdapter(): AccountViewPagerAdapter {
        val fragments =
            arrayListOf(
                GlobalFeedFragment(), MyFeedFragment()
            )
        return AccountViewPagerAdapter(fragments, this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPagerAccount.adapter = null
        _binding = null
    }


}