package de.hbch.traewelling.ui.statusDetail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.hbch.traewelling.BuildConfig
import de.hbch.traewelling.R
import de.hbch.traewelling.api.dtos.Status
import de.hbch.traewelling.databinding.FragmentStatusDetailBinding
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.include.status.CheckInCardViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay

class StatusDetailFragment : Fragment() {

    private lateinit var binding: FragmentStatusDetailBinding
    private val checkInViewModel: CheckInViewModel by activityViewModels()
    private val userViewModel: LoggedInUserViewModel by activityViewModels()
    private val viewModel: StatusDetailViewModel by viewModels()
    private val checkInCardViewModel: CheckInCardViewModel by viewModels()

    private val args: StatusDetailFragmentArgs by navArgs()
    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.status_detail_also_check_in_menu, menu)
            if (isOwnConnection()) {
                menu.getItem(1).isVisible = true
            } else {
                menu.getItem(2).isVisible = true
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.menu_also_check_in -> {
                    alsoCheckIntoThisConnection()
                    true
                }
                R.id.menu_edit_check_in -> {
                    editStatus()
                    true
                }
                R.id.menu_share_check_in -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        val url = "https://traewelling.de/status/${status?.statusId}"
                        type = "text/plain"
                        flags = Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                        putExtra(Intent.EXTRA_TEXT, url)
                    }

                    startActivity(
                        Intent.createChooser(
                            intent,
                            resources.getString(R.string.title_share)
                        )
                    )
                    true
                }
                else -> false
            }
        }
    }

    private val _statusLoading = MutableLiveData(true)
    val statusLoading: LiveData<Boolean> get() = _statusLoading

    private val _statusLoadingSuccess = MutableLiveData(true)
    val statusLoadingSuccess: LiveData<Boolean> get() = _statusLoadingSuccess

    private var status: Status? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatusDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.fragmentStatusContent.setContent {
            MainTheme {
                StatusDetail(
                    modifier = Modifier.fillMaxWidth(),
                    statusId = args.statusId,
                    statusDetailViewModel = viewModel,
                    checkInCardViewModel = checkInCardViewModel,
                    statusLoaded = {
                        status = it
                    }
                )
            }
        }

        requireActivity().addMenuProvider(menuProvider)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().removeMenuProvider(menuProvider)
    }

    private fun isOwnConnection(): Boolean {
        return (userViewModel.loggedInUser.value?.id ?: 0) != args.userId;
    }

    private fun editStatus() {
        if (isOwnConnection())
            return

        val statusValue = status
        if (statusValue != null) {
            findNavController().navigate(
                StatusDetailFragmentDirections.actionStatusDetailFragmentToEditStatusFragment(
                    statusValue.origin,
                    statusValue.destination,
                    statusValue.departurePlanned,
                    statusValue.message,
                    statusValue.line,
                    statusValue.visibility.ordinal,
                    statusValue.business.ordinal,
                    statusValue.statusId,
                    statusValue.hafasTripId,
                    statusValue.originId,
                    category = statusValue.productType
                )
            )
        }
    }

    private fun alsoCheckIntoThisConnection() {
        if (!isOwnConnection())
            return

        val statusValue = status
        if (statusValue != null) {
            checkInViewModel.reset()
            checkInViewModel.lineName = statusValue.line
            checkInViewModel.tripId = statusValue.hafasTripId
            checkInViewModel.startStationId = statusValue.originId
            checkInViewModel.departureTime = statusValue.departurePlanned
            checkInViewModel.destinationStationId = statusValue.destinationId
            checkInViewModel.arrivalTime = statusValue.arrivalPlanned
            checkInViewModel.category = statusValue.productType

            findNavController().navigate(
                StatusDetailFragmentDirections.actionStatusDetailFragmentToCheckInFragment(
                    "",
                    statusValue.destination
                )
            )
        }
    }
}