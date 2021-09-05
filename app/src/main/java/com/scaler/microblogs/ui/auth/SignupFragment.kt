package com.scaler.microblogs.ui.auth

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.scaler.microblogs.R
import com.scaler.microblogs.databinding.FragmentSignupBinding
import com.scaler.microblogs.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SignupFragment : Fragment(R.layout.fragment_signup) {
    private val viewModel: AuthViewModel by viewModels()
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private var isProgressBarVisible: Boolean = false

    @SuppressLint("CommitPrefEdits")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignupBinding.bind(view)

//        binding.apply {
//            buttonSubmitSignup.setOnClickListener {
//                toggleProgressBar()
//                val email = editTextSignupEmail.text.toString().trim()
//                val userName = editTextSignupUsername.text.toString().trim()
//                val password = editTextSignupPassword.text.toString().trim()
//
//                viewModel.signUp(userName, email, password)
//            }
//        }

        viewModel.user.observe(viewLifecycleOwner) {
//            it?.let {
//                toggleProgressBar()
//                Log.i("SignupFragment", "onViewCreated: ${it.token}")
//                AuthModule.authToken = it.token
//                viewModel.setNewUserToken(it.token!!)
//                viewModel.setUserName(it.username!!)
//                findNavController().navigateUp()
//            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {

//            viewModel.authEvent.collect { event ->
//                when (event) {
//                    is AuthEvent.ErrorInUserNameAndPassword -> {
//                        toggleProgressBar()
//                        snackBar("Enter Correct user name or password.")
//                    }
//                    is AuthEvent.ErrorInEmail -> {
//                        toggleProgressBar()
//                        snackBar("Enter Correct email.")
//                    }
//                    is AuthEvent.ErrorInLoginOrSignUp -> {
//                        toggleProgressBar()
//                        snackBar("Error Signing up, Please try again")
//                    }
//                }
//            }
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