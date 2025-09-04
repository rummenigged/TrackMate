package com.octopus.edu.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.octopus.edu.core.ui.common.extensions.setContent

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        setContent {
            HomeScreen(
                onFabClicked = {},
            )
        }
}
