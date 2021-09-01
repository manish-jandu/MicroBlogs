package com.scaler.microblogs.ui.favouritearticles

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.scaler.microblogs.R
import com.scaler.microblogs.databinding.FragmentFavouriteArticlesBinding

class FavouriteArticlesFragment : Fragment(R.layout.fragment_favourite_articles) {
    private var _binding: FragmentFavouriteArticlesBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavouriteArticlesBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
