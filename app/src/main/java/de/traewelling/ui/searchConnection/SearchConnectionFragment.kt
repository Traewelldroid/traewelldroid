package de.traewelling.ui.searchConnection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.traewelling.databinding.FragmentSearchConnectionBinding
import de.traewelling.ui.include.cardSearchStation.SearchStationCard

class SearchConnectionFragment : Fragment() {

    private lateinit var binding: FragmentSearchConnectionBinding
    private lateinit var searchStationCard: SearchStationCard

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchConnectionBinding.inflate(inflater, container, false)
        searchStationCard = SearchStationCard(this, binding.searchCard)
        binding.apply {
            searchCard.viewModel = searchStationCard
        }
        return binding.root
    }
}