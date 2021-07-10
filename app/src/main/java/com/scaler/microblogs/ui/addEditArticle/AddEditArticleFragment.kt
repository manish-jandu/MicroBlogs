package com.scaler.microblogs.ui.addEditArticle

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.scaler.microblogs.R
import com.scaler.microblogs.databinding.FragmentAddEditArticleBinding
import com.scaler.microblogs.ui.addEditArticle.AddEditArticleViewModel.AddEditArticleEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditArticleFragment : Fragment(R.layout.fragment_add_edit_article) {

    private val addEditArticleViewModel: AddEditArticleViewModel by viewModels()
    private var _binding: FragmentAddEditArticleBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAddEditArticleBinding.bind(view)

        binding.buttonSubmitArticle.setOnClickListener {
            submitArticle()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            addEditArticleViewModel.addEditArticleEvent.collect { event->
                when(event){
                    is AddEditArticleEvent.DataIsEmpty ->{
                        showSnackBar("No Field should be Empty!")
                    }
                    is AddEditArticleEvent.ArticleCreated ->{
                        findNavController().navigateUp()
                    }
                    is AddEditArticleEvent.Error ->{
                        showSnackBar("Something went wrong please try again")
                    }
                }
            }
        }
    }

    private fun submitArticle() {
        binding.apply {
            val title = editTextArticleTitle.text.toString().trim()
            val description = editTextArticleAbout.text.toString().trim()
            val body = editTextArticleBody.text.toString().trim()
            val tags = editTextArticleTags.text.toString()

            addEditArticleViewModel.createArticle(title,description,body,tags)
        }
    }

    private fun showSnackBar(message:String){
        Snackbar.make(requireView(),message,Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}