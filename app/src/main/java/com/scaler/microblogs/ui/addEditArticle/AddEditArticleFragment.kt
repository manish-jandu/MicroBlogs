package com.scaler.microblogs.ui.addEditArticle

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.scaler.libconduit.models.Article
import com.scaler.microblogs.R
import com.scaler.microblogs.databinding.FragmentAddEditArticleBinding
import com.scaler.microblogs.utils.Constants.EDITED_ARTICLE
import com.scaler.microblogs.utils.Constants.FRAGMENT_ADD_EDIT_RESULT_REQUEST_KEY
import com.scaler.microblogs.utils.InternetConnectivity.ConnectivityManager
import com.scaler.microblogs.utils.NetworkResult
import com.scaler.microblogs.viewmodels.AddEditArticleViewModel
import com.scaler.microblogs.viewmodels.AddEditArticleViewModel.AddEditArticleEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class AddEditArticleFragment : Fragment(R.layout.fragment_add_edit_article) {

    private val addEditArticleViewModel: AddEditArticleViewModel by viewModels()
    private var _binding: FragmentAddEditArticleBinding? = null
    private val binding get() = _binding!!
    private val args: AddEditArticleFragmentArgs by navArgs()

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAddEditArticleBinding.bind(view)
        setViewLoadingScreen()

        val slug: String? = args.slug
        if (slug == null || slug.isEmpty()) {
            setViewArticleScreen()
        }

        binding.buttonSubmitArticle.setOnClickListener {
            submitArticle()
        }

        observeInternetConnection(slug)
        handleArticleEvents()
    }

    private fun observeInternetConnection(slug: String?) {
        connectivityManager.isNetworkAvailable.observe(viewLifecycleOwner) { it ->
            addEditArticleViewModel.isInternetAvailable = it
            it?.let {
                if (slug != null && slug.isNotEmpty()) {
                    getArticleData(slug)
                }
            }
        }
    }

    private fun getArticleData(slug: String) {
        addEditArticleViewModel.getArticleData(slug)
        addEditArticleViewModel.article.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    setViewArticleScreen()
                    setArticleData(response.data!!)
                }
                is NetworkResult.Loading -> {
                    setViewLoadingScreen()
                }
                is NetworkResult.Error -> {
                    setViewErrorScreen(response.message.toString())
                }
            }
        }
    }

    private fun setArticleData(article: Article) {
        val tags: String? = article.tagList?.joinToString(",")
        binding.apply {
            editTextArticleTitle.editText!!.setText(article.title)
            editTextArticleAbout.editText!!.setText(article.description)
            editTextArticleBody.editText!!.setText(article.body)
            editTextArticleTags.editText!!.setText(tags ?: "")
        }
    }

    private fun handleArticleEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            addEditArticleViewModel.addEditArticleEvent.collect { event ->
                when (event) {
                    is AddEditArticleEvent.ArticleCreated -> {
                        hideLoading()
                        setFragmentResult(
                            FRAGMENT_ADD_EDIT_RESULT_REQUEST_KEY,
                            bundleOf(EDITED_ARTICLE to true)
                        )
                        findNavController().navigateUp()
                    }
                    is AddEditArticleEvent.Error -> {
                        hideLoading()
                        showSnackBar(event.errorMessage)
                    }
                    is AddEditArticleEvent.LoggedOut -> {
                        hideLoading()
                        Toast.makeText(
                            requireContext(),
                            "Login to create Article.",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigateUp()
                    }
                    AddEditArticleEvent.Loading -> {
                        showLoading()
                        //binding.buttonSubmitArticle.oclic
                    }
                }
            }
        }
    }

    private fun submitArticle() {
        binding.apply {
            val title = editTextArticleTitle.editText!!.text.toString().trim()
            val description = editTextArticleAbout.editText!!.text.toString().trim()
            val body = editTextArticleBody.editText!!.text.toString().trim()
            val tags = editTextArticleTags.editText!!.text.toString()

            addEditArticleViewModel.createArticle(title, description, body, tags)
        }
    }

    private fun setViewLoadingScreen() {
        showLoading()
        binding.apply {
            groupShowArticle.visibility = View.GONE
            groupError.visibility = View.GONE
        }
    }

    private fun setViewErrorScreen(message: String) {
        hideLoading()
        binding.apply {
            groupShowArticle.visibility = View.GONE
            groupError.visibility = View.VISIBLE
            textViewError.text = message
        }
    }

    private fun setViewArticleScreen() {
        hideLoading()
        binding.apply {
            groupShowArticle.visibility = View.VISIBLE
            groupError.visibility = View.GONE
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}