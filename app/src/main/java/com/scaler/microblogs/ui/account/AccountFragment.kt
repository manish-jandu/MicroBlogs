package com.scaler.microblogs.ui.account

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.scaler.libconduit.models.User
import com.scaler.microblogs.R
import com.scaler.microblogs.adapters.viewpager.AccountViewPagerAdapter
import com.scaler.microblogs.databinding.FragmentAccountBinding
import com.scaler.microblogs.ui.favouritearticles.FavouriteArticlesFragment
import com.scaler.microblogs.ui.userarticles.UserArticlesFragment
import com.scaler.microblogs.utils.CurrentUserStatus
import com.scaler.microblogs.viewmodels.AccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AccountFragment : Fragment(R.layout.fragment_account) {
    private val accountViewModel: AccountViewModel by activityViewModels()
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()
        accountViewModel.getUserToken()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAccountBinding.bind(view)

        setupButtons()
        setupTabsLayout()
        observeCurrentUserStatus()

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            accountViewModel.accountEvent.collect { event ->
                when (event) {
                    is AccountViewModel.AccountEvent.ErrorLoadingData -> {
                        setViewCurrentlyLoggedOut()
                        Snackbar.make(
                            requireView(),
                            "Please log in again",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    }

    private fun setupButtons() {
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
                setViewCurrentlyLoggedOut()
            }
        }
    }

    private fun observeCurrentUserStatus() {
        accountViewModel.currentUserStatus.observe(viewLifecycleOwner) { currentStatus ->
            currentStatus?.let {
                when (currentStatus) {
                    is CurrentUserStatus.LoggedIn -> {
                        setViewCurrentlyLoggedIn()
                        getCurrentUser()
                        getUserName()
                    }
                    is CurrentUserStatus.LoggedOut -> {
                        setViewCurrentlyLoggedOut()
                    }
                }
            }
        }
    }

    private fun getCurrentUser() {
        accountViewModel.getCurrentUser()
        accountViewModel.currentUser.observe(viewLifecycleOwner) {
            it?.let {
                setCurrentUserData(it)
            }
        }
    }

    private fun getUserName() {
        accountViewModel.getUserName()
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

    private fun setViewCurrentlyLoggedOut() {
        binding.apply {
            groupCurrentlyLoggedIn.visibility = View.GONE
            groupCurrentlyLoggedOut.visibility = View.VISIBLE
        }
    }

    private fun setViewCurrentlyLoggedIn() {
        binding.apply {
            groupCurrentlyLoggedOut.visibility = View.GONE
            groupCurrentlyLoggedIn.visibility = View.VISIBLE
        }
    }

    private fun setupTabsLayout() {
        binding.viewPagerAccount.adapter = getPagerAdapter()

        val titles = arrayListOf("My Posts", "Favourites")
        TabLayoutMediator(
            binding.tabsLayoutCurrentUser,
            binding.viewPagerAccount
        ) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    private fun getPagerAdapter(): AccountViewPagerAdapter {
        val fragments = arrayListOf(UserArticlesFragment(), FavouriteArticlesFragment())
        return AccountViewPagerAdapter(fragments, this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}