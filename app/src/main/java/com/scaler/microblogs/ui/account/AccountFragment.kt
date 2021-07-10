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
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.scaler.libconduit.models.User
import com.scaler.microblogs.databinding.FragmentAccountBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AccountFragment : Fragment() {

    private val accountViewModel: AccountViewModel by viewModels()
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

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
                //Todo: move to edit profile
            }
            buttonSignOut.setOnClickListener {
                accountViewModel.signOut()
            }
        }

        return binding.root
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
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

