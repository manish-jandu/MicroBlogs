package com.scaler.microblogs.ui.userarticles

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.scaler.microblogs.R
import com.scaler.microblogs.databinding.FragmentUserArticlesBinding

class UserArticlesFragment : Fragment(R.layout.fragment_user_articles) {
    private var _binding: FragmentUserArticlesBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserArticlesBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}