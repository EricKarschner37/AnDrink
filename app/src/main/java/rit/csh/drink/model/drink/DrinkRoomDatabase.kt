package rit.csh.drink.model.drink

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import rit.csh.drink.model.User

// Annotates class to be a Room Database with a table (entity) of the Word class
@Database(entities = [Drink::class, Machine::class, User::class], version = 1, exportSchema = false)
abstract class DrinkRoomDatabase : RoomDatabase() {

    abstract fun drinkDao(): DrinkDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: DrinkRoomDatabase? = null

        fun getDatabase(context: Context): DrinkRoomDatabase {
            val tempInstance =
                INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DrinkRoomDatabase::class.java,
                    "machine_drink_table"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}