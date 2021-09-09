package com.scaler.microblogs.ui.article

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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
import com.scaler.libconduit.models.Article
import com.scaler.microblogs.R
import com.scaler.microblogs.adapters.CommentsAdapter
import com.scaler.microblogs.databinding.FragmentArticleBinding
import com.scaler.microblogs.utils.ArticleType
import com.scaler.microblogs.utils.Constants.EDITED_ARTICLE
import com.scaler.microblogs.utils.Constants.FRAGMENT_ADD_EDIT_RESULT_REQUEST_KEY
import com.scaler.microblogs.utils.InternetConnectivity.ConnectivityManager
import com.scaler.microblogs.utils.NetworkResult
import com.scaler.microblogs.viewmodels.ArticleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

private const val TAG = "ArticleFragment"

@AndroidEntryPoint
class ArticleFragment : Fragment(R.layout.fragment_article) {
    private val articleViewModel: ArticleViewModel by viewModels()
    private var _binding: FragmentArticleBinding? = null
    private val binding get() = _binding!!
    private val args: ArticleFragmentArgs by navArgs()
    private val commentAdapter = CommentsAdapter()

    private var isLoggedIn: Boolean = false
    private var isFavourite: Boolean = false

    private lateinit var slug: String
    private lateinit var articleType: ArticleType
    private var isInternetAvaialable: Boolean = false

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentArticleBinding.bind(view)
        observeInternetConnection()

        articleType = args.articleType
        slug = args.slug

        setupRecyclerView()
        setButtonClickListener()
        observeIfLoggedIn()
        observeArticleData()
        observeCommentsData()
        observeResult()
        observeEvents()

        if (articleType == ArticleType.USER_CREATED_ARTICLE) {
            setHasOptionsMenu(true)
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewComments.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeInternetConnection() {
        articleViewModel.isInternetAvailable = connectivityManager.isNetworkAvailable.value
        connectivityManager.isNetworkAvailable.observe(viewLifecycleOwner) {
            it?.let {
                setupArticleView()
                isInternetAvaialable = it
                articleViewModel.isInternetAvailable = it
            }
        }
    }

    private fun observeIfLoggedIn() {
        articleViewModel.checkIfLoggedIn()
        articleViewModel.isLoggedIn.observe(viewLifecycleOwner) {
            it?.let { _isLoggedIn ->
                isLoggedIn = _isLoggedIn
                getArticleData()
                setupArticleView()
                setButtonClickListener()
            }
        }
    }

    private fun getArticleData() {
        if (isLoggedIn) {
            articleViewModel.getArticleDataByAuthRepo(slug)
        } else {
            articleViewModel.getArticleDataByRepo(slug)
        }
    }

    private fun observeArticleData() {
        articleViewModel.article.observe(viewLifecycleOwner) {
            it?.let { response ->
                when (response) {
                    is NetworkResult.Error -> {
                        setViewError(response.message.toString())
                    }
                    is NetworkResult.Loading -> {
                        setViewLoading()
                    }
                    is NetworkResult.Success -> {
                        setupArticleView()
                        setArticleData(response.data!!)
                    }
                }
            }
        }
    }

    private fun observeCommentsData() {
        articleViewModel.getComments(slug)
        articleViewModel.comments.observe(viewLifecycleOwner) {
            it?.let { response ->
                when (response) {
                    is NetworkResult.Error -> {
                        hideLoading()
                        showSnackBar(response.message.toString())
                    }
                    is NetworkResult.Loading -> {
                        showLoading()
                    }
                    is NetworkResult.Success -> {
                        hideLoading()
                        commentAdapter.submitList(response.data)
                    }
                }
            }
        }
    }

    private fun observeResult() {
        setFragmentResultListener(FRAGMENT_ADD_EDIT_RESULT_REQUEST_KEY) { _, bundle ->
            val result = bundle.getBoolean(EDITED_ARTICLE)
            if (result) {
                findNavController().navigateUp()
                Toast.makeText(requireContext(), "Article Updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setArticleData(article: Article) {
        binding.apply {
            textViewTitle.text = article.title
            textViewBody.text = article.body
            article.favorited?.let {
                isFavourite = it
            }
        }
        setFavouriteIcon()//toggle icon color
    }

    private fun setFavouriteIcon() {
        if (isFavourite) {
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

    private fun setButtonClickListener() {
        if (isLoggedIn) {
            binding.imageLikeUnlike.setOnClickListener {
                if (isFavourite) {
                    articleViewModel.unlikeArticle(slug)
                } else {
                    articleViewModel.likeArticle(slug)
                }
                toggleFavourite()
            }
            binding.buttonComment.setOnClickListener {
                createComment(slug)
            }
        } else {
            binding.imageLikeUnlike.setOnClickListener {
                Toast.makeText(requireContext(), "Login and try again!", Toast.LENGTH_SHORT).show()
            }
            binding.buttonComment.setOnClickListener {
                Toast.makeText(requireContext(), "Login and try again!", Toast.LENGTH_SHORT).show()
            }
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

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            articleViewModel.articleEvent.collect { event ->
                when (event) {
                    is ArticleViewModel.ArticleEvent.Error -> {
                        showSnackBar(event.errorMessage)
                    }
                    is ArticleViewModel.ArticleEvent.ErrorLikeUnlike -> {
                        showSnackBar(event.errorMessage)
                        toggleFavourite()
                    }
                }
            }
        }
    }

    private fun toggleFavourite() {
        isFavourite = !isFavourite
        setFavouriteIcon()
    }

    private fun setupArticleView() {
        if (!isInternetAvaialable) {
            setViewError()
        } else {
            setViewArticle()
        }
    }

    private fun setViewLoading() {
        binding.apply {
            groupShowError.visibility = View.INVISIBLE
            groupShowArticle.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun setViewError(message: String = "No Internet connection.") {
        binding.apply {
            textViewError.text = message
            groupShowError.visibility = View.VISIBLE
            groupShowArticle.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE
        }
    }

    private fun setViewArticle() {
        binding.apply {
            groupShowError.visibility = View.INVISIBLE
            groupShowArticle.visibility = View.VISIBLE
            progressBar.visibility = View.INVISIBLE
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.article_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.button_edit_article -> {
                val action =
                    ArticleFragmentDirections.actionArticleFragmentToAddEditArticleFragment(slug)
                findNavController().navigate(action)
            }
            R.id.button_delete_article -> {
                showDeleteDialog(slug)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteDialog(slug: String) {
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