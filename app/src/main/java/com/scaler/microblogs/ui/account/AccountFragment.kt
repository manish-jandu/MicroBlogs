package com.scaler.microblogs.ui.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.scaler.libconduit.models.User
import com.scaler.microblogs.adapters.ArticleAdapter
import com.scaler.microblogs.databinding.FragmentAccountBinding
import com.scaler.microblogs.utils.ArticleType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AccountFragment : Fragment() {

    private val accountViewModel: AccountViewModel by viewModels()
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var userFeedAdapter: ArticleAdapter
    private lateinit var userFavouriteFeedAdapter: ArticleAdapter

    companion object {
        fun newInstance() = AccountFragment()
    }

    override fun onStart() {
        super.onStart()
        accountViewModel.getUserToken()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)

        userFeedAdapter = ArticleAdapter(OnArticleClick(),ArticleType.USER_CREATED_ARTICLE)
        userFavouriteFeedAdapter = ArticleAdapter(OnArticleClick(),ArticleType.ARTICLE)

        accountViewModel.currentUser.observe(viewLifecycleOwner) {
            it?.let {
                setCurrentUserData(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            accountViewModel.accountEvent.collect { event ->
                when (event) {
                    is AccountViewModel.AccountEvent.LoggedIn -> {
                        currentlyLogedIn()
                        Log.i("AccountFragment", "onCreateView: new token is ${event.token}")
                    }
                    is AccountViewModel.AccountEvent.LoggedOut -> {
                        currentlyLoggedOut()
                    }
                    is AccountViewModel.AccountEvent.GotUser -> {
                        observeArticles()
                    }
                    is AccountViewModel.AccountEvent.ErrorLoadingData -> {
                        currentlyLoggedOut()
                        Snackbar.make(
                            requireView(),
                            "Please log in again",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        binding.apply {
            buttonLogin.setOnClickListener {
                val action = AccountFragmentDirections.actionNavAccountToLoginFragment()
                findNavController().navigate(action)
            }
            buttonSignup.setOnClickListener {
                val action = AccountFragmentDirections.actionNavAccountToSignupFragment()
                findNavController().navigate(action)
            }
            buttonEditProfile.setOnClickListener {
                val action = AccountFragmentDirections.actionNavAccountToEditProfileFragment()
                findNavController().navigate(action)
            }
            buttonSignOut.setOnClickListener {
                accountViewModel.signOut()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            recyclerViewUserFavouritesFeed.adapter = userFavouriteFeedAdapter
            recyclerViewUserFavouritesFeed.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewUserFavouritesFeed.visibility = View.GONE

            recyclerViewUserFeed.adapter = userFeedAdapter
            recyclerViewUserFeed.layoutManager = LinearLayoutManager(requireContext())
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

    private fun observeArticles() {
        accountViewModel.userArticles.observe(viewLifecycleOwner) {
            it?.let {
                userFeedAdapter.submitData(viewLifecycleOwner.lifecycle, it)
            }
        }
        accountViewModel.favouriteArticles.observe(viewLifecycleOwner) {
            it?.let {
                userFavouriteFeedAdapter.submitData(viewLifecycleOwner.lifecycle, it)
            }
        }
    }

    private fun currentlyLoggedOut() {
        binding.apply {
            textViewPleaseLoginSignUp.visibility = View.VISIBLE
            buttonLogin.visibility = View.VISIBLE
            buttonSignup.visibility = View.VISIBLE
            buttonSignOut.visibility = View.GONE
            buttonEditProfile.visibility = View.GONE
            textViewUserName.visibility = View.GONE
            textViewUserEmail.visibility = View.GONE
            textViewUserBio.visibility = View.GONE
            cardView.visibility = View.GONE
            tabsLayoutCurrentUser.visibility = View.GONE
            recyclerViewUserFeed.visibility = View.GONE
            recyclerViewUserFavouritesFeed.visibility = View.GONE
        }
    }

    private fun setCurrentUserData(user: User) {
        binding.apply {
            textViewUserName.text = user.username
            textViewUserBio.text = user.bio ?: ""
            textViewUserEmail.text = user.email
            Glide.with(imageViewProfile)
                .load(user.image)
                .centerCrop()
                .into(imageViewProfile)
        }
    }

    private fun currentlyLogedIn() {
        binding.apply {
            textViewPleaseLoginSignUp.visibility = View.GONE
            buttonLogin.visibility = View.GONE
            buttonSignup.visibility = View.GONE
            buttonSignOut.visibility = View.VISIBLE
            buttonEditProfile.visibility = View.VISIBLE
            textViewUserName.visibility = View.VISIBLE
            textViewUserEmail.visibility = View.VISIBLE
            textViewUserBio.visibility = View.VISIBLE
            cardView.visibility = View.VISIBLE
            tabsLayoutCurrentUser.visibility = View.VISIBLE
            recyclerViewUserFeed.visibility = View.VISIBLE
            recyclerViewUserFavouritesFeed.visibility = View.GONE
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

