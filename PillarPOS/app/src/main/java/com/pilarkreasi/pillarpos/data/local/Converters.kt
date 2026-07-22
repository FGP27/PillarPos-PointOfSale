package com.pilarkreasi.pillarpos.data.local

import androidx.room.TypeConverter
import com.pilarkreasi.pillarpos.data.model.DiscountType
import com.pilarkreasi.pillarpos.data.model.Role
import java.util.Date

class Converters {
    @TypeConverter
    fun fromRole(role: Role): String = role.name

    @TypeConverter
    fun toRole(value: String): Role = Role.valueOf(value)

    @TypeConverter
    fun fromDiscountType(type: DiscountType): String = type.name

    @TypeConverter
    fun toDiscountType(value: String): DiscountType = DiscountType.valueOf(value)

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}