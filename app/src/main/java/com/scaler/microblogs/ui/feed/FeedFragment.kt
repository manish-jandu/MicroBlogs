package com.scaler.microblogs.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.scaler.microblogs.adapters.ArticleAdapter
import com.scaler.microblogs.databinding.FragmentFeedBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

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

        feedViewModel.getUserToken()

        val currentUserAdapter = ArticleAdapter()
        val globalAdapter = ArticleAdapter()

        binding.apply {
            recyclerViewCurrentUserFeed.adapter = currentUserAdapter
            recyclerViewCurrentUserFeed.layoutManager = LinearLayoutManager(requireContext())

            recyclerViewGlobalFeed.adapter = globalAdapter
            recyclerViewGlobalFeed.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewGlobalFeed.visibility = View.GONE
        }

        val tabsLayout = binding.tabsLayout
        val myFeed = tabsLayout.getTabAt(0)
        val globalFeed = tabsLayout.getTabAt(1)


        feedViewModel.globalArticles.observe(viewLifecycleOwner) {
            globalAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            feedViewModel.feedEvent.collect { event ->
                when (event) {
                    is FeedViewModel.FeedEvent.LoggedIn -> {
                        binding.recyclerViewCurrentUserFeed.visibility = View.VISIBLE
                        feedViewModel.feedArticles.observe(viewLifecycleOwner) {
                            currentUserAdapter.submitData(viewLifecycleOwner.lifecycle, it)
                        }
                    }
                    is FeedViewModel.FeedEvent.LoggedOut -> {
                        binding.recyclerViewCurrentUserFeed.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "Logged out,login again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        binding.tabsLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab) {
                    myFeed -> {
                        binding.recyclerViewGlobalFeed.visibility = View.GONE
                        binding.recyclerViewCurrentUserFeed.visibility = View.VISIBLE
                    }
                    globalFeed -> {
                        binding.recyclerViewGlobalFeed.visibility = View.VISIBLE
                        binding.recyclerViewCurrentUserFeed.visibility = View.GONE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                when (tab) {
                    myFeed -> {
                    }
                    globalFeed -> {
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                when (tab) {
                    myFeed -> {
                    }
                    globalFeed -> {
                    }
                }
            }

        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}