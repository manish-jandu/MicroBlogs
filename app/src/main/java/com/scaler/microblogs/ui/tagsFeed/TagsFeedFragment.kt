package com.scaler.microblogs.ui.tagsFeed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.scaler.microblogs.adapters.ArticleAdapter
import com.scaler.microblogs.databinding.FragmentTagsBinding
import com.scaler.microblogs.utils.ArticleType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TagsFeedFragment : Fragment() {

    private val feedViewModel: TagsFeedViewModel by viewModels()
    private var _binding: FragmentTagsBinding? = null
    private val binding get() = _binding!!
    private val tagsFeedFragmentArgs: TagsFeedFragmentArgs by navArgs()

    companion object {
        fun newInstance() = TagsFeedFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTagsBinding.inflate(inflater, container, false)

        val tagToGetArticle = tagsFeedFragmentArgs.tag

        val adapter = ArticleAdapter(OnArticleClick(),ArticleType.ARTICLE)
        binding.recyclerViewTags.adapter = adapter
        binding.recyclerViewTags.layoutManager = LinearLayoutManager(requireContext())

        feedViewModel.getArticlesByTag(tagToGetArticle)

        feedViewModel.articleByTag.observe(viewLifecycleOwner) {
            it?.let {
                adapter.submitData(viewLifecycleOwner.lifecycle, it)
            }
        }

        return binding.root
    }

    inner class OnArticleClick() : ArticleAdapter.OnArticleClick {
        override fun onItemClick(slug: String, articleType: ArticleType) {
            val action =
                TagsFeedFragmentDirections.actionTagsFeedFragmentToArticleFragment(articleType, slug)
            findNavController().navigate(action)
        }

        override fun onProfileClick(userName: String) {
            val action = TagsFeedFragmentDirections.actionTagsFeedFragmentToProfileFragment(userName)
            findNavController().navigate(action)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}