package de.hbch.traewelling.ui.checkIn

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.transition.MaterialContainerTransform
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.databinding.FragmentCheckInBinding
import de.hbch.traewelling.shared.CheckInViewModel
import de.hbch.traewelling.shared.EventViewModel
import de.hbch.traewelling.shared.SharedValues

abstract class AbstractCheckInFragment : Fragment() {

    protected lateinit var binding: FragmentCheckInBinding
    protected val checkInViewModel: CheckInViewModel by activityViewModels()
    protected val eventViewModel: EventViewModel by activityViewModels()
    private lateinit var secureStorage: SecureStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCheckInBinding.inflate(inflater, container, false)

        secureStorage = SecureStorage(requireContext())
        val storedHashtag = secureStorage.getObject(SharedValues.SS_HASHTAG, String::class.java)
        if (storedHashtag != null && storedHashtag != "") {
            checkInViewModel.message.postValue("\n#${storedHashtag}")
        }

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            scrimColor = Color.TRANSPARENT
            val color = TypedValue()
            requireContext().theme.resolveAttribute(android.R.attr.windowBackground, color, true)
            if (color.type >= TypedValue.TYPE_FIRST_COLOR_INT && color.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                setAllContainerColors(color.data)
            }
        }
        return binding.root
    }

    open fun onChangeDestination() = Unit
    abstract fun submit()
}