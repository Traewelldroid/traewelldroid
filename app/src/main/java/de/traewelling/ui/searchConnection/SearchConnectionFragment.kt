package de.traewelling.ui.searchConnection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.Hold
import de.traewelling.R
import de.traewelling.adapters.ConnectionAdapter
import de.traewelling.api.TraewellingApi
import de.traewelling.api.models.trip.HafasTripPage
import de.traewelling.databinding.FragmentSearchConnectionBinding
import de.traewelling.models.Connection
import de.traewelling.ui.include.cardSearchStation.SearchStationCard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SearchConnectionFragment : Fragment() {

    private lateinit var binding: FragmentSearchConnectionBinding
    private lateinit var searchStationCard: SearchStationCard
    private val args: SearchConnectionFragmentArgs by navArgs()
    private val viewModel: SearchConnectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = Hold()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSearchConnectionBinding.inflate(inflater, container, false)

        val connectionRecyclerView = binding.recyclerViewConnections
        connectionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        connectionRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        viewModel.departures.observe(viewLifecycleOwner) { connections ->
            binding.recyclerViewConnections.adapter =
                ConnectionAdapter(connections.data) { itemView, connection ->
                    val transitionName = connection.tripId
                    val extras = FragmentNavigatorExtras(itemView to transitionName)
                    val action =
                        SearchConnectionFragmentDirections.actionSearchConnectionFragmentToSelectDestinationFragment(
                            transitionName
                        )
                    findNavController().navigate(action, extras)
                }
        }
        viewModel.searchConnections(args.stationName, Date())

        binding.stationName = args.stationName
        searchStationCard = SearchStationCard(this, binding.searchCard, args.stationName)

        binding.apply {
            searchCard.viewModel = searchStationCard
            viewModel = (this@SearchConnectionFragment).viewModel
        }
        return binding.root
    }


}