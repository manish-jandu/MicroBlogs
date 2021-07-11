package com.scaler.microblogs.ui.article

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.scaler.microblogs.R
import com.scaler.microblogs.adapters.CommentsAdapter
import com.scaler.microblogs.databinding.FragmentArticleBinding
import com.scaler.microblogs.utils.ArticleType
import com.scaler.microblogs.utils.Constants.EDITED_ARTICLE
import com.scaler.microblogs.utils.Constants.FRAGMENT_ADD_EDIT_RESULT_REQUEST_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

private const val TAG = "ArticleFragment"

@AndroidEntryPoint
class ArticleFragment : Fragment(R.layout.fragment_article) {
    private val articleViewModel: ArticleViewModel by viewModels()
    private var _binding: FragmentArticleBinding? = null
    private val binding get() = _binding!!
    private val args: ArticleFragmentArgs by navArgs()
    val commentAdapter = CommentsAdapter()

    private var isLoggedIn: Boolean = false
    private var isFavourite: Boolean = false
    private lateinit var slug: String
    private lateinit var articleType: ArticleType

    override fun onStart() {
        super.onStart()
        articleViewModel.checkIfLoggedIn()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentArticleBinding.bind(view)

        articleType = args.articleType
        slug = args.slug

        articleViewModel.checkIfLoggedIn()
        articleViewModel.getComments(slug)

        setViewForTypeOfArticle()
        setUpArticle()

        articleViewModel.isLoggedIn.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    isLoggedIn = true
                    currentlyLoggedIn()
                    articleViewModel.getArticleDataByAuthRepo(slug)
                } else {
                    isLoggedIn = false
                    currentlyLoggedOut()
                    articleViewModel.getArticleDataByRepo(slug)
                }
            }
        }

        articleViewModel.article.observe(viewLifecycleOwner) {
            it?.let {
                binding.apply {
                    textViewTitle.text = it.title
                    textViewBody.text = it.body
                    it.favorited?.let {
                        isFavourite = it
                        setFavourite(isFavourite)
                    }
                }
            }
        }

        articleViewModel.comments.observe(viewLifecycleOwner) {
            it?.let {
                commentAdapter.submitList(it.comments)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            articleViewModel.articleEvent.collect { event ->
                when (event) {
                    is ArticleViewModel.ArticleEvent.Error -> {
                        findNavController().navigateUp()
                        Toast.makeText(requireContext(), "Try Again!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        //refresh
        setFragmentResultListener(FRAGMENT_ADD_EDIT_RESULT_REQUEST_KEY) { _, bundle ->
            val result = bundle.getBoolean(EDITED_ARTICLE)
            if (result) {
                findNavController().navigateUp()
                Toast.makeText(requireContext(), "Article Updated", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setViewForTypeOfArticle() {
        binding.apply {
            if (articleType == ArticleType.USER_CREATED_ARTICLE) {
                buttonEditArticle.visibility = View.VISIBLE
                buttonDeleteArticle.visibility = View.VISIBLE
            } else {
                buttonEditArticle.visibility = View.GONE
                buttonDeleteArticle.visibility = View.GONE
            }
        }
    }

    private fun setUpArticle() {
        binding.apply {
            recyclerViewComments.adapter = commentAdapter
            recyclerViewComments.layoutManager = LinearLayoutManager(requireContext())

            buttonDeleteArticle.setOnClickListener {
                showDialog(slug)
            }
            buttonEditArticle.setOnClickListener {
                val action =
                    ArticleFragmentDirections.actionArticleFragmentToAddEditArticleFragment(slug)
                findNavController().navigate(action)
            }

        }

    }

    private fun currentlyLoggedIn() {
        binding.imageLikeUnlike.setOnClickListener {
            if (isFavourite) {
                isFavourite =!isFavourite
                setFavourite(isFavourite)
                articleViewModel.unlikeArticle(slug)
            } else {
                isFavourite =!isFavourite
                setFavourite(isFavourite)
                articleViewModel.likeArticle(slug)
            }
        }
        binding.buttonComment.setOnClickListener {
            createComment(slug)
        }
    }

    private fun currentlyLoggedOut() {
        binding.imageLikeUnlike.setOnClickListener {
            Toast.makeText(requireContext(), "Login and try again!", Toast.LENGTH_SHORT).show()
        }
        binding.buttonComment.setOnClickListener {
            Toast.makeText(requireContext(), "Login and try again!", Toast.LENGTH_SHORT).show()

        }
    }

    private fun setFavourite(isFavourite: Boolean?) {
        if (isFavourite == true) {
            Glide.with(binding.imageLikeUnlike)
                .load(R.drawable.ic_liked)
                .centerCrop()
                .into(binding.imageLikeUnlike)
        } else {
            Glide.with(binding.imageLikeUnlike)
                .load(R.drawable.ic_unliked)
                .centerCrop()
                .into(binding.imageLikeUnlike)
        }
    }

    private fun createComment(slug: String) {
        binding.apply {
            val comment = editTextComment.text.toString().trim()
            if (comment.isEmpty()) {
                Snackbar.make(requireView(), "Comment cannot be empty", Snackbar.LENGTH_SHORT)
                    .show()
            } else {
                editTextComment.setText("")
                editTextComment.clearFocus()
                articleViewModel.createComment(slug, comment)
            }
        }
    }

    private fun showDialog(slug: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Alert!")
            .setMessage("Do you really want to delete this Article")
            .setPositiveButton("Delete") { _, _ ->
                articleViewModel.deleteArticle(slug)
                findNavController().navigateUp()
                Toast.makeText(requireContext(), "Article deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { _, _ ->

            }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}