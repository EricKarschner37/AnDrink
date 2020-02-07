package rit.csh.andrink.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DrinkDao {

    @Transaction
    @Query("SELECT * FROM Machine")
    fun getMachinesWithDrinks(): LiveData<List<MachineWithDrinks>>

    @Query("SELECT * FROM USER WHERE isCurrent")
    fun getCurrentUser(): LiveData<User>

    @Query("DELETE FROM Machine")
    suspend fun deleteMachines()

    @Query("DELETE FROM Drink")
    suspend fun deleteDrinks()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg drinks: Drink)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg machines: Machine)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

}