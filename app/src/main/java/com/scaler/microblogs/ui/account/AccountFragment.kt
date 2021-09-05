package com.scaler.microblogs.ui.account

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.scaler.libconduit.models.User
import com.scaler.microblogs.R
import com.scaler.microblogs.adapters.viewpager.AccountViewPagerAdapter
import com.scaler.microblogs.databinding.FragmentAccountBinding
import com.scaler.microblogs.ui.favouritearticles.FavouriteArticlesFragment
import com.scaler.microblogs.ui.userarticles.UserArticlesFragment
import com.scaler.microblogs.utils.CurrentUserStatus
import com.scaler.microblogs.utils.InternetConnectivity.ConnectivityManager
import com.scaler.microblogs.utils.NetworkResult
import com.scaler.microblogs.viewmodels.AccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccountFragment : Fragment(R.layout.fragment_account) {
    private val accountViewModel: AccountViewModel by activityViewModels()
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAccountBinding.bind(view)

        setViewLoading()
        setupButtons()
        setupTabsLayout()
        observeInternetConnection()
        observeCurrentUserStatus()
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
            }
        }
    }

    private fun observeInternetConnection() {
        connectivityManager.isNetworkAvailable.observe(viewLifecycleOwner) { it ->
            it?.let {
                accountViewModel.isInternetAvailable = it
                setupView()
            }
        }
    }

    private fun observeCurrentUserStatus() {
        accountViewModel.updateCurrentUserStatus()
        accountViewModel.currentUserStatus.observe(viewLifecycleOwner) { currentStatus ->
            currentStatus?.let {
                setupView()
                if (currentStatus == CurrentUserStatus.LoggedIn) {
                    getUserName()
                    getCurrentUserData()
                }
            }
        }
    }

    private fun setupView() {
        val isInternetAvailable = accountViewModel.isInternetAvailable
        val currentUserStatus = accountViewModel.currentUserStatus.value

        if (isInternetAvailable != null && currentUserStatus != null) {

            if (isInternetAvailable == false) {
                setViewError()
                Toast.makeText(requireContext(), "No Internet Connection.", Toast.LENGTH_SHORT)
                    .show()
            } else if (isInternetAvailable == true && currentUserStatus == CurrentUserStatus.LoggedIn) {
                //setViewCurrentlyLoggedIn()
            } else if (isInternetAvailable == true && currentUserStatus == CurrentUserStatus.LoggedOut) {
                setViewCurrentlyLoggedOut()
            }

        }
    }


    private fun getCurrentUserData() {
        accountViewModel.getCurrentUser()
        accountViewModel.currentUser.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    setViewCurrentlyLoggedIn()
                    setCurrentUserData(response.data!!)
                }
                is NetworkResult.Error -> {
                    setViewError(response.message.toString())
                }
                is NetworkResult.Loading -> {
                    setViewLoading()
                }
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
            groupError.visibility = View.GONE
            progressBar.visibility = View.GONE
        }
    }

    private fun setViewCurrentlyLoggedIn() {
        binding.apply {
            groupCurrentlyLoggedOut.visibility = View.GONE
            groupCurrentlyLoggedIn.visibility = View.VISIBLE
            groupError.visibility = View.GONE
            progressBar.visibility = View.GONE
        }
    }

    private fun setViewError(message: String = "No Internet Connection.") {
        binding.apply {
            groupCurrentlyLoggedIn.visibility = View.GONE
            groupCurrentlyLoggedOut.visibility = View.GONE
            groupError.visibility = View.VISIBLE
            progressBar.visibility = View.GONE

            textViewError.text = message
        }
    }

    private fun setViewLoading() {
        binding.apply {
            groupCurrentlyLoggedIn.visibility = View.GONE
            groupCurrentlyLoggedOut.visibility = View.GONE
            groupError.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
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