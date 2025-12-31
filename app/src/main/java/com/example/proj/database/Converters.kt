// File: com/example/proj/database/Converters.kt
package com.example.proj.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    private val gson = Gson()

    // 1. Date converters - FOR USER ENTITY
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // 2. List<String> converters - FOR USER ENTITY (enrolledCourses, completedCourses)
    @TypeConverter
    fun fromString(value: String?): List<String> {
        if (value.isNullOrBlank()) {
            return emptyList()
        }
        return try {
            val listType = object : TypeToken<ArrayList<String>>() {}.type
            gson.fromJson<ArrayList<String>>(value, listType) ?: emptyList<String>()
        } catch (e: Exception) {
            emptyList<String>()
        }
    }

    @TypeConverter
    fun fromList(list: List<String>?): String {
        return try {
            gson.toJson(list ?: emptyList<String>())
        } catch (e: Exception) {
            "[]"
        }
    }
}