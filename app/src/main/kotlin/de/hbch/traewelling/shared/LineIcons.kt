package de.hbch.traewelling.shared

import de.hbch.traewelling.api.models.lineIcons.LineIcon

class LineIcons private constructor() {

    val icons: MutableList<LineIcon> = mutableListOf()

    companion object {
        private var instance: LineIcons? = null

        fun getInstance() = instance ?: LineIcons().also { instance = it }
    }
}