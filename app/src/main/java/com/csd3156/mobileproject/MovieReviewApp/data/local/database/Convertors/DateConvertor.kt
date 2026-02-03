package com.csd3156.mobileproject.MovieReviewApp.data.local.database.Convertors

import androidx.room.TypeConverter
import java.util.Date

class DateConvertor {
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(millisSinceEpoch: Long?): Date? {
        return millisSinceEpoch?.let { Date(it) }
    }

}
