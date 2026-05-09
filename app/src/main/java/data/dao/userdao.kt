package data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import data.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // إضافة مستخدم جديد (Register)
    @Insert(onConflict = OnConflictStrategy.ABORT) // لو الإيميل موجود هيطلع Error مش هيمسح القديم
    suspend fun insertUser(user: User): Long

    // البحث عن مستخدم بالإيميل (عشان الـ Login)
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    // جلب بيانات مستخدم بالـ ID
    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getUserById(userId: Int): User?

    // userdao.kt
    @Query("SELECT COUNT(*) FROM users")
    fun getTotalUsersCount(): Flow<Int>  // ← Flow مش suspend

    @Query("SELECT COUNT(*) FROM users WHERE role = 'tenant'")
    fun getTotalTenantsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE role = 'landlord'")
    fun getTotalLandlordsCount(): Flow<Int>

    @Query("SELECT * FROM users WHERE role != 'admin'")
    fun getAllUsersForAdmin(): Flow<List<User>>

    @Query("DELETE FROM users WHERE user_id = :userId")
    suspend fun deleteUserById(userId: Int)
    // تحديث بيانات (زي تغيير الباسورد أو الاسم)


    @Query("UPDATE users SET profile_image_path = :imagePath WHERE user_id = :userId")
    suspend fun updateProfileImagePath(userId: Int, imagePath: String?)

    @Update
    suspend fun updateUser(user: User)

    // مسح الحساب
    @Delete
    suspend fun deleteUser(user: User)
}

