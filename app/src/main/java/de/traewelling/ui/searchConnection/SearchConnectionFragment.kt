package de.traewelling.ui.searchConnection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.traewelling.databinding.FragmentSearchConnectionBinding

class SearchConnectionFragment : Fragment() {

    private lateinit var binding: FragmentSearchConnectionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchConnectionBinding.inflate(inflater, container, false)
        return binding.root
    }
}