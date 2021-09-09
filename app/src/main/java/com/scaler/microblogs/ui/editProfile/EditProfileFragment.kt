package com.scaler.microblogs.ui.editProfile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.scaler.libconduit.models.User
import com.scaler.microblogs.R
import com.scaler.microblogs.databinding.FragmentEditProfileBinding
import com.scaler.microblogs.utils.InternetConnectivity.ConnectivityManager
import com.scaler.microblogs.utils.NetworkResult
import com.scaler.microblogs.viewmodels.EditProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {
    private val editProfileViewModel: EditProfileViewModel by viewModels()
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditProfileBinding.bind(view)

        setViewLoading()
        observeInternetConnection()
        observeUpdatedUserData()
        observeEvents()

        binding.buttonSubmitEditedProfile.setOnClickListener {
            updateData()
        }
    }

    private fun observeInternetConnection() {
        editProfileViewModel.isInternetAvailable= connectivityManager.isNetworkAvailable.value
        connectivityManager.isNetworkAvailable.observe(viewLifecycleOwner) { it ->
            it?.let {
                editProfileViewModel.isInternetAvailable = it
                observeCurrentUser()
            }
        }
    }

    private fun observeCurrentUser() {
        editProfileViewModel.getCurrentUser()
        editProfileViewModel.currentUser.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Loading -> {
                    setViewLoading()
                }
                is NetworkResult.Success -> {
                    setViewCurrentUser()
                    setDataInFields(response.data!!)
                }
                is NetworkResult.Error -> {
                    setViewError(response.message)
                }
            }
        }
    }

    private fun observeUpdatedUserData() {
        editProfileViewModel.updatedUser.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    showLoading()
                }
                is NetworkResult.Success -> {
                    hideLoading()
                    editProfileViewModel.setUserDataInPreferences(result.data!!)
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
            editProfileViewModel.editProfileEvent.collect { event ->
                when (event) {
                    EditProfileViewModel.EditProfileEvent.Success -> {
                        findNavController().navigateUp()
                    }
                }
            }
        }
    }

    private fun updateData() {
        binding.apply {
            val username = editTextEditUserName.editText!!.text.toString().trim()
            val bio = editTextEditBio.editText!!.text.toString().trim()
            val imageUrl = editTextEditImageUrl.editText!!.text.toString().trim()
            val email = editTextEditEmail.editText!!.text.toString().trim()
            val password = editTextEditPassword.editText!!.text.toString().trim()

            editProfileViewModel.updateUserData(username, bio, imageUrl, email, password)
        }
    }

    private fun setDataInFields(user: User) {
        binding.apply {
            editTextEditUserName.editText!!.setText(user.username)
            editTextEditBio.editText!!.setText(user.bio ?: "")
            editTextEditEmail.editText!!.setText(user.email)

            if (user.image == null) {
                editTextEditImageUrl.editText!!.setText("")
            } else {
                editTextEditImageUrl.editText!!.setText(user.image.toString())
            }
        }
    }

    private fun setViewLoading() {
        binding.apply {
            groupCurrentUser.visibility = View.INVISIBLE
            groupError.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun setViewError(message: String?) {
        binding.apply {
            textViewError.text = message
            groupError.visibility = View.VISIBLE
            groupCurrentUser.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE
        }
    }

    private fun setViewCurrentUser() {
        binding.apply {
            groupCurrentUser.visibility = View.VISIBLE
            groupError.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE
        }
    }

    private fun showLoading() {
        binding.buttonSubmitEditedProfile.visibility = View.INVISIBLE
        binding.progressBarButton.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.buttonSubmitEditedProfile.visibility = View.VISIBLE
        binding.progressBarButton.visibility = View.INVISIBLE
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}