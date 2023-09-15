package de.hbch.traewelling.util

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import de.hbch.traewelling.api.models.lineIcons.LineIcon
import de.hbch.traewelling.api.models.lineIcons.LineIconShape
import java.io.InputStream

fun readCsv(inputStream: InputStream): List<LineIcon> = csvReader().open(inputStream) {
    readAllWithHeaderAsSequence().map {
        LineIcon(
            it["shortOperatorName"]!!.trim(),
            it["lineName"]!!.trim(),
            it["hafasOperatorCode"]?.trim(),
            it["hafasLineId"]!!.trim(),
            it["backgroundColor"]!!.trim(),
            it["textColor"]!!.trim(),
            LineIconShape.valueOf(it["shape"]!!.trim().replace('-', '_'))
        )
    }.toList()
}
