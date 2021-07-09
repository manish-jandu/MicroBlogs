package com.scaler.microblogs.ui.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.scaler.microblogs.databinding.FragmentAccountBinding
import dagger.hilt.android.AndroidEntryPoint

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

        binding.buttonLogin.setOnClickListener {
            val action = AccountFragmentDirections.actionNavAccountToLoginFragment()
            findNavController().navigate(action)
        }
        binding.buttonSignup.setOnClickListener {
            val action = AccountFragmentDirections.actionNavAccountToSignupFragment()
            findNavController().navigate(action)
        }

        accountViewModel.userToken.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {

            } else {
                binding.apply {
                    textViewPleaseLoginSignUp.visibility = View.GONE
                    buttonLogin.visibility = View.GONE
                    buttonSignup.visibility = View.GONE
                }
                Log.i("AccountFragment", "onCreateView: new token is $it")
            }
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

