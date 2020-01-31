package rit.csh.andrink.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DrinkDao {

    @Transaction
    @Query("SELECT * FROM Machine")
    fun getMachinesWithDrinks(): LiveData<List<MachineWithDrinks>>

    @Query("DELETE FROM Drink")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg drinks: Drink)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg machines: Machine)

}