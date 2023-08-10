package de.hbch.traewelling.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ZonedDateTimeGsonConverter : JsonSerializer<ZonedDateTime>, JsonDeserializer<ZonedDateTime> {
    override fun serialize(src: ZonedDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(FORMATTER.format(src))
    }
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ZonedDateTime? {
        return ZonedDateTime.parse(json!!.asString).withZoneSameInstant(ZoneId.systemDefault())
    }
    companion object {
        private val FORMATTER = DateTimeFormatter.ISO_INSTANT
    }
}

class ZonedDateTimeRetrofitConverterFactory private constructor() : Converter.Factory() {
    companion object {
        fun create(): ZonedDateTimeRetrofitConverterFactory {
            return ZonedDateTimeRetrofitConverterFactory()
        }
    }

    override fun stringConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? {
        if (type == ZonedDateTime::class.java) {
            return ZonedDateTimeRetrofitConverter.INSTANCE
        }
        return super.stringConverter(type, annotations, retrofit)
    }

    class ZonedDateTimeRetrofitConverter private constructor(): Converter<ZonedDateTime, String> {
        companion object {
            val INSTANCE = ZonedDateTimeRetrofitConverter()
        }

        override fun convert(value: ZonedDateTime): String {
            return DateTimeFormatter.ISO_INSTANT.format(value)
        }
    }
}
