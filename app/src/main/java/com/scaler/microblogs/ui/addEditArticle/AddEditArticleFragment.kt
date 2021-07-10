package com.scaler.microblogs.ui.addEditArticle

import android.os.Bundle
import android.view.View
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
import com.scaler.microblogs.ui.addEditArticle.AddEditArticleViewModel.AddEditArticleEvent
import com.scaler.microblogs.utils.Constants.EDITED_ARTICLE
import com.scaler.microblogs.utils.Constants.FRAGMENT_ADD_EDIT_RESULT_REQUEST_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditArticleFragment : Fragment(R.layout.fragment_add_edit_article) {

    private val addEditArticleViewModel: AddEditArticleViewModel by viewModels()
    private var _binding: FragmentAddEditArticleBinding? = null
    private val binding get() = _binding!!
    private val args: AddEditArticleFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAddEditArticleBinding.bind(view)

        val slug: String? = args.slug

        if (!slug.isNullOrEmpty()) {
            addEditArticleViewModel.getArticleData(slug)
        }

        binding.buttonSubmitArticle.setOnClickListener {
            submitArticle()
        }

        addEditArticleViewModel.article.observe(viewLifecycleOwner) {
            it?.let {
                setArticleData(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            addEditArticleViewModel.addEditArticleEvent.collect { event ->
                when (event) {
                    is AddEditArticleEvent.DataIsEmpty -> {
                        showSnackBar("No Field should be Empty!")
                    }
                    is AddEditArticleEvent.ArticleCreated -> {
                        setFragmentResult(
                            FRAGMENT_ADD_EDIT_RESULT_REQUEST_KEY,
                            bundleOf(EDITED_ARTICLE to true)
                        )
                        findNavController().navigateUp()
                    }
                    is AddEditArticleEvent.Error -> {
                        showSnackBar("Something went wrong please try again")
                    }
                }
            }
        }
    }

    private fun setArticleData(article: Article) {
        binding.apply {
            editTextArticleTitle.setText(article.title)
            editTextArticleAbout.setText(article.description)
            editTextArticleBody.setText(article.body)
            val tags: String? = article.tagList?.joinToString(",")
            editTextArticleTags.setText(tags ?: "")
        }
    }

    private fun submitArticle() {
        binding.apply {
            val title = editTextArticleTitle.text.toString().trim()
            val description = editTextArticleAbout.text.toString().trim()
            val body = editTextArticleBody.text.toString().trim()
            val tags = editTextArticleTags.text.toString()

            addEditArticleViewModel.createArticle(title, description, body, tags)
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