package com.csd3156.mobileproject.MovieReviewApp.data.local.database.Convertors

import androidx.room.TypeConverter
import java.util.UUID

class UUIDConvertor {
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUUID(uuidString: String?): UUID? {
        return uuidString?.let { UUID.fromString(it) }
    }
}