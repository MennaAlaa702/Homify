package data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.Executors
import data.entities.User
import data.entities.LandlordProfile
import data.entities.TenantProfile
import data.entities.Unit
import data.dao.UserDao
import data.dao.ProfileDao
import data.dao.UnitDao

// 1. تحديد الجداول ونسخة قاعدة البيانات
@Database(
    entities = [
        User::class,
        LandlordProfile::class,
        TenantProfile::class,
        Unit::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // 2. تعريف الـ DAOs للوصول للعمليات
    abstract fun userDao(): UserDao
    abstract fun profileDao(): ProfileDao
    abstract fun unitDao(): UnitDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "homify_database"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // إضافة بيانات الأدمن عند إنشاء الداتابيز لأول مرة فقط
                            Executors.newSingleThreadExecutor().execute {
                                db.execSQL("""
    INSERT INTO users (first_name, last_name, email, national_id, password, role) 
    VALUES ('Admin', 'User', 'admin@homify.com', '00000000000000', 'admin333', 'admin')
""".trimIndent())
                            }
                        }
                    })
                    .fallbackToDestructiveMigration() // بيمسح الداتا لو غيرتي في الـ Schema (مفيد جداً وقت التطوير)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

