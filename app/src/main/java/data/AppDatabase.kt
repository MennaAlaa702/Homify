package data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.Executors
import data.entities.User
import data.entities.LandlordProfile
import data.entities.TenantProfile
import data.entities.Unit
import data.dao.UserDao
import data.dao.ProfileDao
import data.dao.UnitDao

@Database(
    entities = [
        User::class,
        LandlordProfile::class,
        TenantProfile::class,
        Unit::class,
    ],
    version = 2,          //  رفعنا الـ version من 1 إلى 2
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun profileDao(): ProfileDao
    abstract fun unitDao(): UnitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        //  Migration من version 1 إلى 2: بنضيف العمود الجديد
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE users ADD COLUMN profile_image_path TEXT"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "homify_database"
                )
                    .addMigrations(MIGRATION_1_2)   // ✅ بدل fallbackToDestructiveMigration
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Executors.newSingleThreadExecutor().execute {
                                db.execSQL("""
    INSERT INTO users (first_name, last_name, email, national_id, password, role, profile_image_path) 
    VALUES ('Admin', 'User', 'admin@homify.com', '00000000000000', 'admin333', 'admin', NULL)
""".trimIndent())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}