package de.hbch.traewelling.api.models

data class Data<T>(
    val data: T
)

data class DataList<T>(
    val data: List<T>
)