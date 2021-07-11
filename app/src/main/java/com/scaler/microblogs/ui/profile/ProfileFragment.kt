package com.scaler.microblogs.ui.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.scaler.libconduit.models.Profile
import com.scaler.microblogs.R
import com.scaler.microblogs.adapters.ArticleAdapter
import com.scaler.microblogs.databinding.FragmentProfileBinding
import com.scaler.microblogs.utils.ArticleType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val profileViewModel: ProfileViewModel by viewModels()

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var userFeedAdapter: ArticleAdapter
    private lateinit var userFavouriteFeedAdapter: ArticleAdapter
    private val args: ProfileFragmentArgs by navArgs()

    private var isLoggedIn: Boolean = false
    private var isFollowing: Boolean = false
    private lateinit var userName: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        userFeedAdapter = ArticleAdapter(OnArticleClick(), ArticleType.ARTICLE)
        userFavouriteFeedAdapter = ArticleAdapter(OnArticleClick(), ArticleType.ARTICLE)

        userName = args.userName
        profileViewModel.getUser(userName)
        profileViewModel.checkIfLoggedIn()

        setupRecyclerView()

        profileViewModel.isLoggedIn.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    isLoggedIn = true
                    profileViewModel.getProfileFromAuthRepo(userName)
                    currentlyLoggedIn()

                } else {
                    isLoggedIn = false
                    profileViewModel.getProfileFromRepo(userName)
                    currentlyLoggedOut()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            profileViewModel.profileEvent.collect { event ->
                when (event) {
                    is ProfileViewModel.ProfileEvent.StartObserving -> {
                        startObserving()
                    }
                    is ProfileViewModel.ProfileEvent.LoggedOut -> {
                        currentlyLoggedOut()
                    }
                }
            }
        }

        profileViewModel.profile.observe(viewLifecycleOwner) {
            it?.let {
                setProfile(it)
            }
        }

        val tabsLayout = binding.tabsLayoutCurrentUser
        val userFeed = tabsLayout.getTabAt(0)
        val userFavouriteFeed = tabsLayout.getTabAt(1)

        binding.tabsLayoutCurrentUser.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab) {
                    userFeed -> {
                        binding.recyclerViewUserFavouritesFeed.visibility = View.GONE
                        binding.recyclerViewUserFeed.visibility = View.VISIBLE
                    }
                    userFavouriteFeed -> {
                        binding.recyclerViewUserFavouritesFeed.visibility = View.VISIBLE
                        binding.recyclerViewUserFeed.visibility = View.GONE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                when (tab) {
                    userFeed -> {
                    }
                    userFavouriteFeed -> {
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                when (tab) {
                    userFeed -> {
                    }
                    userFavouriteFeed -> {
                    }
                }
            }

        })

    }

    private fun currentlyLoggedIn() {
        binding.buttonFollowUnfollow.text = "Follow"
        binding.buttonFollowUnfollow.setOnClickListener {
            profileViewModel.followUnfollowAccount(userName,isFollowing)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun currentlyLoggedOut() {
        binding.buttonFollowUnfollow.text = "UnFollow"
        binding.buttonFollowUnfollow.setOnClickListener {
            Toast.makeText(requireContext(), "Currently Logged out", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setProfile(profile: Profile) {
        binding.apply {
            textViewUserName.text = profile.username
            textViewUserBio.text = profile.bio ?: ""
            if (isLoggedIn) {
                if (profile.following!!) {
                    isFollowing = true
                    binding.buttonFollowUnfollow.text = "UnFollow"
                } else {
                    isFollowing=false
                    binding.buttonFollowUnfollow.text = "Follow"
                }
            }
            Glide.with(imageViewProfile)
                .load(profile.image)
                .centerCrop()
                .into(imageViewProfile)
        }
    }

    private fun setupRecyclerView() {
        binding.apply {
            recyclerViewUserFeed.adapter = userFeedAdapter
            recyclerViewUserFeed.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewUserFeed.visibility = View.VISIBLE

            recyclerViewUserFavouritesFeed.adapter = userFavouriteFeedAdapter
            recyclerViewUserFavouritesFeed.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewUserFavouritesFeed.visibility = View.GONE
        }
    }

    private fun startObserving() {
        profileViewModel.userArticles.observe(viewLifecycleOwner) {
            it?.let {
                userFeedAdapter.submitData(viewLifecycleOwner.lifecycle, it)
            }
        }
        profileViewModel.favouriteArticles.observe(viewLifecycleOwner) {
            it?.let {
                userFavouriteFeedAdapter.submitData(viewLifecycleOwner.lifecycle, it)
            }
        }
    }

    inner class OnArticleClick() : ArticleAdapter.OnArticleClick {
        override fun onItemClick(slug: String, articleType: ArticleType) {
           val action =
                ProfileFragmentDirections.actionProfileFragmentToArticleFragment(articleType, slug)
            findNavController().navigate(action)
        }

        override fun onProfileClick(userName: String) {
            val action = ProfileFragmentDirections.actionProfileFragmentSelf(userName)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


