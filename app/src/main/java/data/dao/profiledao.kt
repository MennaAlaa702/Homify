package data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import data.entities.LandlordProfile
import data.entities.TenantProfile

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLandlordProfile(profile: LandlordProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenantProfile(profile: TenantProfile)

    @Query("SELECT * FROM landlord_profiles WHERE user_id = :userId")
    suspend fun getLandlordByUserId(userId: Int): LandlordProfile?

    @Query("SELECT * FROM tenant_profiles WHERE user_id = :userId")
    suspend fun getTenantByUserId(userId: Int): TenantProfile?
}