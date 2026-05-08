package data.entities

import android.R
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.ColumnInfo
import androidx.room.Index

enum class UnitType {
    Near_Uni, Studio, Villa, Apartment, Shared_Room
}
@Entity(
    tableName = "units",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["user_id"],
            childColumns = ["landlord_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["landlord_id"])]
)
data class Unit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "landlord_id")
    val landlordId: Int,
    val title: String,
    val description: String,
    val governorate: String,
    val address: String,
    @ColumnInfo(name = "location_link")
    val locationLink: String,
    val price: Double,
    @ColumnInfo(name = "unit_type")
    val unitType: UnitType,
    val amenities: String,
    val bedrooms: Int,
    val bathrooms: Int,
    val size: Int,
    val images: String, // هنا بنخزن روابط الصور مفصولة بفاصلة مثلاً، أو رابط الصورة الأساسية
)
