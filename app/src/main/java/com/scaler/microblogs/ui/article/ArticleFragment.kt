package com.scaler.microblogs.ui.article

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.scaler.microblogs.R
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
    private val args : ArticleFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentArticleBinding.bind(view)

        val articleType = args.articleType
        val slug = args.slug

        articleViewModel.getArticleData(slug)

        Log.i(TAG, "onViewCreated: type $articleType")
        Log.i(TAG, "onViewCreated: slug $slug")

        binding.apply {
            if(articleType == ArticleType.USER_CREATED_ARTICLE){
                buttonEditArticle.visibility = View.VISIBLE
                buttonDeleteArticle.visibility = View.VISIBLE
            }else{
                buttonEditArticle.visibility = View.GONE
                buttonDeleteArticle.visibility = View.GONE
            }
        }

        binding.apply {
            buttonDeleteArticle.setOnClickListener {
                showDialog(slug)
            }
            buttonEditArticle.setOnClickListener {
                val action = ArticleFragmentDirections.actionArticleFragmentToAddEditArticleFragment(slug)
                findNavController().navigate(action)
            }
        }

        articleViewModel.article.observe(viewLifecycleOwner){
            it?.let {
                binding.apply {
                    textViewTitle.text = it.title
                    textViewBody.text = it.body
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            articleViewModel.articleEvent.collect {event->
                when(event){
                    is ArticleViewModel.ArticleEvent.Error ->{
                        findNavController().navigateUp()
                        Toast.makeText(requireContext(),"Try Again!",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        //refresh
        setFragmentResultListener(FRAGMENT_ADD_EDIT_RESULT_REQUEST_KEY){_,bundle->
            val result =bundle.getBoolean(EDITED_ARTICLE)
            if(result){
                findNavController().navigateUp()
                Toast.makeText(requireContext(),"Article Updated",Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(),"Article deleted",Toast.LENGTH_SHORT).show()
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