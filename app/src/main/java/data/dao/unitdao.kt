package data.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import data.entities.Unit
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {
    @Insert
    suspend fun insertUnit(unit: Unit)
    @Query("SELECT * FROM units ORDER BY id DESC") // الأحدث
    fun getUnitsLatest(): Flow<List<Unit>>

    @Query("SELECT * FROM units ORDER BY price ASC") // الأرخص
    fun getUnitsCheapest(): Flow<List<Unit>>

    @Query("SELECT * FROM units ORDER BY price DESC") // الأغلى
    fun getUnitsMostExpensive(): Flow<List<Unit>>

    @Query("SELECT * FROM units ORDER BY title ASC") // الأبجدي
    fun getUnitsAlphabetical(): Flow<List<Unit>>

    @Query("SELECT * FROM units WHERE governorate = :gov")
    suspend fun getUnitsByGovernorate(gov: String): List<Unit>

    @Query("SELECT COUNT(*) FROM units WHERE landlord_id = :lId")
    suspend fun getUnitCountForLandlord(lId: Int): Int

    //    @Query("SELECT COUNT(*) FROM units")
//    suspend fun getTotalUnitsCount(): Int
// unitdao.kt
    @Query("SELECT COUNT(*) FROM units")
    fun getTotalUnitsCount(): Flow<Int>  // ← Flow مش suspend
    @Query("SELECT * FROM units")
    fun getAllUnitsForAdmin(): Flow<List<Unit>>

    @Query("SELECT * FROM units WHERE id = :unitId")
    suspend fun getUnitById(unitId: Int): Unit?

    @Query("SELECT * FROM units WHERE landlord_id = :landlordId")
    fun getUnitsByLandlord(landlordId: Int): Flow<List<Unit>>

    @Query("DELETE FROM units WHERE id = :unitId")
    suspend fun deleteUnitById(unitId: Int)
    @Update
    suspend fun updateUnit(unit: Unit)

    @Delete
    suspend fun deleteUnit(unit: Unit)

}