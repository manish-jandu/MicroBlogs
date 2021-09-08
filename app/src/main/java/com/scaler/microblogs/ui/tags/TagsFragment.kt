package com.scaler.microblogs.ui.tags

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.scaler.microblogs.R
import com.scaler.microblogs.adapters.TagsAdapter
import com.scaler.microblogs.databinding.FragmentTagsBinding
import com.scaler.microblogs.utils.InternetConnectivity.ConnectivityManager
import com.scaler.microblogs.utils.NetworkResult
import com.scaler.microblogs.viewmodels.TagsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TagsFragment : Fragment(R.layout.fragment_tags) {
    private val tagsViewModel: TagsViewModel by viewModels()
    private var _binding: FragmentTagsBinding? = null
    private val binding get() = _binding!!
    private val tagAdapter = TagsAdapter(OnTagClick())

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTagsBinding.bind(view)

        setViewLoading()
        binding.recyclerViewTags.apply {
            adapter = tagAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        observeInternetConnection()
    }

    private fun observeInternetConnection() {
        connectivityManager.isNetworkAvailable.observe(viewLifecycleOwner) {
            it?.let { isInternetAvailable ->
                if (isInternetAvailable) {
                    observeTagsResponse()
                } else {
                    setViewError()
                }
            }
        }
    }

    private fun observeTagsResponse() {
        tagsViewModel.getTags()
        tagsViewModel.tags.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    setViewTags()
                    setDataInTags(response.data!!)
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

    private fun setDataInTags(data: List<String>) {
        tagAdapter.submitList(data)
    }

    private fun setViewLoading() {
        binding.apply {
            textViewError.visibility = View.INVISIBLE
            imageViewError.visibility = View.INVISIBLE
            recyclerViewTags.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun setViewError(message: String = "No Internet Connection.") {
        binding.apply {
            textViewError.text = message
            textViewError.visibility = View.VISIBLE
            imageViewError.visibility = View.VISIBLE
            recyclerViewTags.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE
        }
    }

    private fun setViewTags() {
        binding.apply {
            textViewError.visibility = View.INVISIBLE
            imageViewError.visibility = View.INVISIBLE
            recyclerViewTags.visibility = View.VISIBLE
            progressBar.visibility = View.INVISIBLE
        }
    }

    inner class OnTagClick : TagsAdapter.OnItemTagClick {
        override fun onTagClick(tag: String) {
            val action =
                TagsFragmentDirections.actionNavTagsToTagsFeedFragment(tag)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}