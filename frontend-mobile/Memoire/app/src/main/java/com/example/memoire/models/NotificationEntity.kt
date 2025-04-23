package com.example.memoire.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class NotificationEntity(
    val id: Long,
    val userId: Long,
    val type: String,
    val text: String,
    val relatedItemId: Long,
    val itemType: String,
    var isRead: Boolean,

    @JsonAdapter(LocalDateTimeStringAdapter::class)
    val createdAt: LocalDateTime
)


class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime> {
    private val formatters = listOf(
        // Try with milliseconds first
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
        // Then without milliseconds
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        // ISO format
        DateTimeFormatter.ISO_LOCAL_DATE_TIME
    )

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalDateTime {
        val dateString = json.asString

        for (formatter in formatters) {
            try {
                return LocalDateTime.parse(dateString, formatter)
            } catch (e: DateTimeParseException) {
                // Try next format
            }
        }

        throw DateTimeParseException(
            "Unable to parse date: $dateString",
            dateString, 0
        )
    }
}

class LocalDateTimeStringAdapter : TypeAdapter<LocalDateTime>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(formatter.format(value))
        }
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): LocalDateTime {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                throw IOException("Null value for LocalDateTime")
            }
            else -> {
                val dateString = reader.nextString()
                LocalDateTime.parse(dateString, formatter)
            }
        }
    }
}