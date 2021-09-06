package com.scaler.microblogs.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.scaler.microblogs.R
import com.scaler.microblogs.databinding.FragmentSignupBinding
import com.scaler.microblogs.utils.InternetConnectivity.ConnectivityManager
import com.scaler.microblogs.utils.NetworkResult
import com.scaler.microblogs.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject


@AndroidEntryPoint
class SignupFragment : Fragment(R.layout.fragment_signup) {
    private val viewModel: AuthViewModel by viewModels()
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignupBinding.bind(view)

        setupSignupButton()
        observeInternetConnection()
        observeUser()
        observeEvents()
    }

    private fun setupSignupButton() {
        binding.apply {
            buttonSubmitSignup.setOnClickListener {
                editTextSignupPassword.error = null

                val email = editTextSignupEmail.editText!!.text.toString().trim()
                val userName = editTextSignupUsername.editText!!.text.toString().trim()
                val password = editTextSignupPassword.editText!!.text.toString().trim()

                if (password.length < 8) {
                    editTextSignupPassword.error = "Password should be min. 8 char"
                } else {
                    viewModel.signUp(userName, email, password)
                }
            }
        }
    }

    private fun observeInternetConnection() {
        connectivityManager.isNetworkAvailable.observe(viewLifecycleOwner) {
            viewModel.isInternetAvailable = it
        }
    }

    private fun observeUser() {
        viewModel.user.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    viewModel.setUserDataInPreferences(result.data!!)
                    hideLoading()
                }
                is NetworkResult.Loading -> {
                    showLoading()
                }
                is NetworkResult.Error -> {
                    hideLoading()
                    showSnackBar(result.message.toString())
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.authEvent.collect { event ->
                when (event) {
                    AuthViewModel.AuthEvent.SetUserDataSuccess -> {
                        findNavController().navigateUp()
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.apply {
            buttonSubmitSignup.visibility = View.GONE
            progressCircularSignUp.visibility = View.VISIBLE
        }
    }

    private fun hideLoading() {
        binding.apply {
            buttonSubmitSignup.visibility = View.VISIBLE
            progressCircularSignUp.visibility = View.GONE
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}