package data
import androidx.room.TypeConverter
import data.entities.UnitType
import data.entities.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name
    @TypeConverter
    fun toUserRole(role: String): UserRole = UserRole.valueOf(role)

    @TypeConverter
    fun fromUnitType(type: UnitType): String = type.name
    @TypeConverter
    fun toUnitType(type: String): UnitType = UnitType.valueOf(type)
}
