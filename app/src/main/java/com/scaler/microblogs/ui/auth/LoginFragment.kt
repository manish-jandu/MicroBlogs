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
import com.scaler.microblogs.di.AuthModule
import com.scaler.microblogs.viewmodels.AuthViewModel
import com.scaler.microblogs.viewmodels.AuthViewModel.AuthEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {
    private val viewModel: AuthViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var isProgressBarVisible: Boolean = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)
        binding.apply {
            buttonSubmitLogin.setOnClickListener {
                toggleProgressBar()
                val email = editTextLoginEmail.text.toString().trim()
                val password = editTextLoginPassword.text.toString().trim()

                viewModel.login(email, password)
            }
        }

        viewModel.user.observe(viewLifecycleOwner) {
            it?.let {
                toggleProgressBar()
                AuthModule.authToken = it.token
                viewModel.setNewUserToken(it.token!!)
                viewModel.setUserName(it.username!!)
                findNavController().navigateUp()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {

            viewModel.authEvent.collect { event ->
                when (event) {
                    is AuthEvent.ErrorInEmail -> {
                        toggleProgressBar()
                        snackBar("Enter Correct email.")
                    }
                    is AuthEvent.ErrorInLoginPassword -> {
                        toggleProgressBar()
                        snackBar("Enter Password")
                    }
                    is AuthEvent.ErrorInLoginOrSignUp -> {
                        toggleProgressBar()
                        snackBar("Error Logging in, Please try again")
                    }
                }
            }
        }
    }

    private fun snackBar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun toggleProgressBar() {
        isProgressBarVisible = !isProgressBarVisible
        binding.progressCircularSignUp.visibility =
            if (isProgressBarVisible) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}