package rit.csh.andrink.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DrinkDao {

    @Query("SELECT * from drink_table WHERE machine LIKE :machine_query ORDER BY cost")
    fun getDrinksForMachine(machine_query: String): LiveData<List<Drink>>

    @Query("DELETE FROM drink_table")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(drink: Drink)


}