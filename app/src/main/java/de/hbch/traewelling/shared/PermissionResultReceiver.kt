package de.hbch.traewelling.shared

interface PermissionResultReceiver {
    fun onPermissionResult(isGranted: Boolean)
}