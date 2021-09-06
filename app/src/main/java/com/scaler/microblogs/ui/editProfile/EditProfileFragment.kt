package com.scaler.microblogs.ui.editProfile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.scaler.libconduit.models.User
import com.scaler.microblogs.R
import com.scaler.microblogs.databinding.FragmentEditProfileBinding
import com.scaler.microblogs.viewmodels.EditProfileViewModel
import com.scaler.microblogs.viewmodels.EditProfileViewModel.EditProfileEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private val editProfileViewModel: EditProfileViewModel by viewModels()
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()
        editProfileViewModel.getUserToken()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditProfileBinding.bind(view)

        binding.buttonSubmitEditedProfile.setOnClickListener {
            updateData()
        }

        editProfileViewModel.currentUser.observe(viewLifecycleOwner) {
            it?.let {
                setDataInFields(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            editProfileViewModel.editProfileEvent.collect { event ->
                when (event) {
                    is EditProfileEvent.ErrorLoadingData -> {
                        findNavController().navigateUp()
                        Toast.makeText(
                            requireContext(),
                            "Try again after logging in",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is EditProfileEvent.ErrorInUserNameAndPassword -> {
                        showSnackBar("Please enter valid username and password")
                    }
                    is EditProfileEvent.ErrorInEmail -> {
                        showSnackBar("Please enter valid email")
                    }
                    is EditProfileEvent.ErrorInUpdatingData -> {
                        showSnackBar("Error while updating data try again!")
                    }
                    is EditProfileEvent.SuccessFullyUpdatedData -> {
                        findNavController().navigateUp()
                        Toast.makeText(
                            requireContext(),
                            "Successfully update data",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
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
            editTextEditBio.editText!!.setText(user.bio ?: "")
            editTextEditEmail.editText!!.setText(user.email)
            if (user.image == null) {
                editTextEditImageUrl.editText!!.setText("")
            } else {
                editTextEditImageUrl.editText!!.setText(user.image.toString())
            }
            editTextEditUserName.editText!!.setText(user.username)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}