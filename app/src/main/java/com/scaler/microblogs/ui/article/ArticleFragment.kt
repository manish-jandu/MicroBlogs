package com.scaler.microblogs.ui.article

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.scaler.microblogs.R
import com.scaler.microblogs.databinding.FragmentArticleBinding
import com.scaler.microblogs.utils.ArticleType
import dagger.hilt.android.AndroidEntryPoint

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

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}