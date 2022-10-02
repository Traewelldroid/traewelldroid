package de.hbch.traewelling.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import de.hbch.traewelling.shared.UserViewModel

class UserFragment : AbstractUserFragment() {

    override val viewModel: UserViewModel by activityViewModels()
    private val args: UserFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.loadUser(args.userName) {
            loadCheckIns()
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}