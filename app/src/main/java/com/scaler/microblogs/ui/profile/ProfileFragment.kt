package com.scaler.microblogs.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.scaler.libconduit.models.Profile
import com.scaler.microblogs.R
import com.scaler.microblogs.adapters.ArticleAdapter
import com.scaler.microblogs.adapters.viewpager.AccountViewPagerAdapter
import com.scaler.microblogs.databinding.FragmentProfileBinding
import com.scaler.microblogs.ui.favouritearticles.FavouriteArticlesFragment
import com.scaler.microblogs.ui.userarticles.UserArticlesFragment
import com.scaler.microblogs.utils.ArticleType
import com.scaler.microblogs.utils.InternetConnectivity.ConnectivityManager
import com.scaler.microblogs.utils.NetworkResult
import com.scaler.microblogs.viewmodels.ProfileViewModel
import com.scaler.microblogs.viewmodels.ProfileViewModel.ProfileEvent.ErrorFollowUnFollow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val profileViewModel: ProfileViewModel by viewModels()
    private val args: ProfileFragmentArgs by navArgs()
    private lateinit var userName: String
    private var isLoggedIn = false
    private var isFollowing = false

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        userName = args.userName
        setViewLoading()
        observeInternetConnection()
        observeIfLoggedIn()
        observeProfile()
        observeProfileEvents()
        setupTabsLayout(userName)
    }

    private fun observeInternetConnection() {
        connectivityManager.isNetworkAvailable.observe(viewLifecycleOwner) {
            it?.let { isInternetAvailable ->
                profileViewModel.isInternetAvailable = isInternetAvailable
                setupView()
            }
        }
    }

    private fun observeIfLoggedIn() {
        profileViewModel.checkIfLoggedIn()
        profileViewModel.isLoggedIn.observe(viewLifecycleOwner) {
            it?.let { _isLoggedIn ->
                setLoggedIn(_isLoggedIn)
                setFollowUnfollowButton()
                getProfile()
            }
        }
    }

    private fun setLoggedIn(isLoggedIn: Boolean) {
        this.isLoggedIn = isLoggedIn
    }

    private fun setFollowUnfollowButton() {
        setFollowUnfollowButtonText()
        if (isLoggedIn) {
            binding.buttonFollowUnfollow.setOnClickListener {
                profileViewModel.followUnfollowAccount(userName, isFollowing)
                toggleFollowUnfollow()
            }
        } else {
            binding.buttonFollowUnfollow.setOnClickListener {
                Toast.makeText(requireContext(), "Currently logged out.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setFollowUnfollowButtonText() {
        binding.buttonFollowUnfollow.text = if (isFollowing) "UnFollow" else "Follow"
    }

    private fun toggleFollowUnfollow() {
        this.isFollowing = !isFollowing
        setFollowUnfollowButtonText()
    }

    private fun getProfile() {
        if (isLoggedIn) {
            profileViewModel.getProfileFromAuthRepo(userName)
        } else {
            profileViewModel.getProfileFromRepo(userName)
        }
    }

    private fun observeProfile() {
        profileViewModel.profile.observe(viewLifecycleOwner) { it ->
            it?.let { response ->
                when (response) {
                    is NetworkResult.Loading -> {
                        setViewLoading()
                    }
                    is NetworkResult.Success -> {
                        setViewProfile()
                        setProfile(response.data!!)
                    }
                    is NetworkResult.Error -> {
                        setViewError(response.message.toString())
                    }
                }
            }
        }
    }

    private fun observeProfileEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            profileViewModel.profileEvent.collect { event ->
                when (event) {
                    //error while follow unfollow operation.
                    //toggle follow unfollow
                    is ErrorFollowUnFollow -> {
                        toggleFollowUnfollow()
                        showSnackBar(event.errorMessage)
                    }
                }
            }
        }
    }

    private fun setProfile(profile: Profile) {
        binding.apply {
            textViewUserName.text = profile.username
            textViewUserBio.text = profile.bio ?: "bio:"
            profile.following?.let { following ->
                setIsFollowing(following)
                setFollowUnfollowButtonText()
            }
            Glide.with(imageViewProfile)
                .load(profile.image)
                .centerCrop()
                .into(imageViewProfile)
        }
    }

    private fun setIsFollowing(following: Boolean) {
        this.isFollowing = following
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun setupView() {
        val isInternetAvailable = profileViewModel.isInternetAvailable
        val isLoggedIn = profileViewModel.isLoggedIn.value
        val currentProfileHasData = profileViewModel.profile.value != null

        if (isInternetAvailable != null && isLoggedIn != null) {
            if (!isInternetAvailable) {
                setViewError()
            } else if (currentProfileHasData) {
                setViewProfile()
            } else {
                setViewLoading()
            }
        }
    }

    private fun setViewError(message: String = "No Internet Connection.") {
        binding.apply {
            textViewError.text = message
            groupProfile.visibility = View.INVISIBLE
            groupError.visibility = View.VISIBLE
            progressBar.visibility = View.INVISIBLE
        }
    }

    private fun setViewLoading() {
        binding.apply {
            groupProfile.visibility = View.INVISIBLE
            groupError.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun setViewProfile() {
        binding.apply {
            groupProfile.visibility = View.VISIBLE
            groupError.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE
        }
    }

    private fun setupTabsLayout(userName: String) {
        binding.viewPagerAccount.adapter = getPagerAdapter(userName)

        val titles = arrayListOf("My Posts", "Favourites")
        TabLayoutMediator(
            binding.tabsLayoutCurrentUser,
            binding.viewPagerAccount
        ) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    private fun getPagerAdapter(
        userName: String,
    ): AccountViewPagerAdapter {
        val articleType = ArticleType.ARTICLE
        val fragments =
            arrayListOf(
                UserArticlesFragment(userName, articleType, OnArticleClick()),
                FavouriteArticlesFragment(userName, articleType, OnArticleClick())
            )
        return AccountViewPagerAdapter(fragments, this)
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


