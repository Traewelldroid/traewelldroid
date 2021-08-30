package de.traewelling.ui.launcher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LauncherActivityViewModel : ViewModel() {
    private val _launcherEvent = MutableLiveData<LaunchAction>()
    val launcherEvent: LiveData<LaunchAction> get() = _launcherEvent

    init {
        _launcherEvent.value = LaunchAction.LOGIN
    }
}