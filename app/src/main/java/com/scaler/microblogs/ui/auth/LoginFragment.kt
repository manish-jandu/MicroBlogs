package com.scaler.microblogs.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.scaler.microblogs.R
import com.scaler.microblogs.databinding.FragmentLoginBinding
import com.scaler.microblogs.utils.InternetConnectivity.ConnectivityManager
import com.scaler.microblogs.utils.NetworkResult
import com.scaler.microblogs.viewmodels.AuthViewModel
import com.scaler.microblogs.viewmodels.AuthViewModel.AuthEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {
    private val viewModel: AuthViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        setupLoginButton()
        observeInternetConnection()
        observeUser()
        observeEvents()
    }

    private fun setupLoginButton() {
        binding.apply {
            buttonSubmitLogin.setOnClickListener {
                val email = editTextLoginEmail.editText?.text.toString().trim()
                val password = editTextLoginPassword.editText?.text.toString().trim()
                viewModel.login(email, password)
            }
        }
    }

    private fun observeInternetConnection() {
        viewModel.isInternetAvailable= connectivityManager.isNetworkAvailable.value
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
                    AuthEvent.SetUserDataSuccess -> {
                        findNavController().navigateUp()
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.apply {
            progressCircular.visibility = View.VISIBLE
            buttonSubmitLogin.visibility = View.GONE
        }
    }

    private fun hideLoading() {
        binding.apply {
            progressCircular.visibility = View.GONE
            buttonSubmitLogin.visibility = View.VISIBLE
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