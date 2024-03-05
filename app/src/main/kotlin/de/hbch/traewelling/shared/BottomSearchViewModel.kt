package de.hbch.traewelling.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.user.User

class BottomSearchViewModel : ViewModel() {
    private var _clickHandler: ((String) -> Unit)? = null

    private val _userResults = MutableLiveData<List<User>?>(null)
    val userResults: LiveData<List<User>?> get() = _userResults

    private val _displayResults = MutableLiveData(false)
    val displayResults: LiveData<Boolean> get() = _displayResults

    suspend fun searchUsers(query: String) {
        _displayResults.postValue(true)
        val data = try { TraewellingApi.userService.searchUsers(query).data } catch (_: Exception) { listOf() }
        _userResults.postValue(data)
    }

    fun reset() {
        _displayResults.postValue(false)
        _userResults.postValue(null)
        _clickHandler = null
    }

    fun registerClickHandler(handler: (String) -> Unit) {
        _clickHandler = handler
    }

    fun onClick(selection: String) {
        _clickHandler?.invoke(selection)
    }
}