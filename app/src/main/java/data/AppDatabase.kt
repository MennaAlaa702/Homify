package data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.homify.R
import kotlinx.coroutines.runBlocking
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
    version = 3,
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

        // ════════════════════════════════════════════
        //  Migrations using Context to avoid Hardcoding
        // ════════════════════════════════════════════

        private fun getMigration12(context: Context): Migration {
            return object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    // يستخدم "alter_table" الموجود في ملفك
                    database.execSQL(context.getString(R.string.alter_table))
                }
            }
        }

        private fun getMigration23(context: Context): Migration {
            return object : Migration(2, 3) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    seedDatabase(database, context)
                }
            }
        }

        // ════════════════════════════════════════════
        //  Seed Database Logic
        // ════════════════════════════════════════════
        private fun seedDatabase(db: SupportSQLiteDatabase, context: Context) {
            val res = context.resources

            // 1. Insert Admin
            db.execSQL(String.format(
                context.getString(R.string.sql_insert_user_template),
                res.getString(R.string.seed_admin_first_name),
                res.getString(R.string.seed_admin_last_name),
                res.getString(R.string.admin_email),
                res.getString(R.string.seed_admin_nid),
                res.getString(R.string.seed_admin_pass),
                res.getString(R.string.role_admin_lower)
            ))

            // 2. Insert Landlord
            val landlordEmail = res.getString(R.string.seed_landlord_email)
            db.execSQL(String.format(
                context.getString(R.string.sql_insert_user_template),
                res.getString(R.string.seed_landlord_first_name),
                res.getString(R.string.seed_landlord_last_name),
                landlordEmail,
                res.getString(R.string.seed_landlord_nid),
                res.getString(R.string.seed_landlord_pass),
                res.getString(R.string.role_landlord_lower)
            ))

            // 3. Insert Landlord Profile
            db.execSQL(String.format(
                context.getString(R.string.sql_insert_landlord_profile_template),
                res.getString(R.string.seed_landlord_phone),
                landlordEmail
            ))

            // 4. Insert Units (جمع الوحدات في جملة واحدة)
            val unitsData = """
                ${formatUnitRow(context, R.string.seed_unit1_title, R.string.seed_unit1_desc, R.string.gov_cairo, R.string.seed_unit1_address, 2500.0, R.string.unit_type_studio, R.string.amenities_wifi_parking, 1, 1, 45, R.string.seed_drawable_home)}
                UNION ALL ${formatUnitRow(context, R.string.seed_unit2_title, R.string.seed_unit2_desc, R.string.gov_giza, R.string.seed_unit2_address, 15000.0, R.string.unit_type_villa, R.string.amenities_villa, 4, 3, 350, R.string.seed_drawable_home2)}
                UNION ALL ${formatUnitRow(context, R.string.seed_unit3_title, R.string.seed_unit3_desc, R.string.gov_alexandria, R.string.seed_unit3_address, 800.0, R.string.unit_type_shared_room, R.string.wifi, 1, 1, 20, R.string.seed_drawable_home3)}
                UNION ALL ${formatUnitRow(context, R.string.seed_unit4_title, R.string.seed_unit4_desc, R.string.gov_cairo, R.string.seed_unit4_address, 5000.0, R.string.apartment, R.string.amenities_apt, 2, 2, 120, R.string.seed_drawable_home)}
                UNION ALL ${formatUnitRow(context, R.string.seed_unit5_title, R.string.seed_unit5_desc, R.string.gov_mansoura, R.string.seed_unit5_address, 1200.0, R.string.unit_type_near_uni, R.string.amenities_laundry, 1, 1, 30, R.string.seed_drawable_home2)}
            """.trimIndent()

            db.execSQL(String.format(
                context.getString(R.string.sql_insert_units_from_view_template),
                unitsData,
                landlordEmail
            ))
        }

        private fun formatUnitRow(context: Context, title: Int, desc: Int, gov: Int, addr: Int, price: Double, type: Int, am: Int, bed: Int, bath: Int, size: Int, img: Int): String {
            return String.format(
                context.getString(R.string.sql_unit_select_row_template),
                context.getString(title), context.getString(desc), context.getString(gov),
                context.getString(addr), context.getString(R.string.seed_map_link),
                price, context.getString(type), context.getString(am),
                bed, bath, size, context.getString(img)
            )
        }

        private fun ensureSeedData(db: AppDatabase, context: Context) {
            val adminEmail = context.getString(R.string.admin_email)
            val admin = runBlocking { db.userDao().getUserByEmail(adminEmail) }
            if (admin == null) {
                seedDatabase(db.openHelper.writableDatabase, context)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            val appContext = context.applicationContext
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    appContext.getString(R.string.db_name)
                )
                    .addMigrations(getMigration12(appContext), getMigration23(appContext))
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Executors.newSingleThreadExecutor().execute {
                                seedDatabase(db, appContext)
                            }
                        }
                    })
                    .build()

                INSTANCE = instance
                Executors.newSingleThreadExecutor().execute {
                    ensureSeedData(instance, appContext)
                }
                instance
            }
        }
    }
}