package de.traewelling.ui.user

import StandardListItemAdapter
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.jcloquell.androidsecurestorage.SecureStorage
import de.traewelling.R
import de.traewelling.databinding.FragmentUserBinding
import de.traewelling.shared.SharedValues
import de.traewelling.ui.login.LoginActivity


class UserFragment : Fragment() {

    private lateinit var binding: FragmentUserBinding
    private val viewModel: UserFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@UserFragment.viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuItems = listOf(
            MenuItem(R.string.logout, R.drawable.ic_logout) {
                val secureStorage = SecureStorage(requireContext())
                secureStorage.removeObject(SharedValues.SS_JWT)
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            }
        )
        binding.recyclerViewMenu.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMenu.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.recyclerViewMenu.adapter = StandardListItemAdapter(menuItems, {item, binding ->
            binding.imageId = item.drawable
            binding.title = getString(item.title)
        }, {
            it.action()
        })
    }
}

class MenuItem(val title: Int, val drawable: Int, val action: () -> Unit) {}