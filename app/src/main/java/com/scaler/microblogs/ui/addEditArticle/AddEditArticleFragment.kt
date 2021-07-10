package com.scaler.microblogs.ui.addEditArticle

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.scaler.microblogs.R
import com.scaler.microblogs.databinding.FragmentAddEditArticleBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditArticleFragment : Fragment(R.layout.fragment_add_edit_article) {

    private val addEditArticleViewModel: AddEditArticleViewModel by viewModels()
    private var _binding: FragmentAddEditArticleBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}