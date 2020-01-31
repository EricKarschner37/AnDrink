package rit.csh.andrink.model

import androidx.lifecycle.LiveData

class DrinkRepository(private val drinkDao: DrinkDao) {

    val machines = drinkDao.getMachinesWithDrinks()

    suspend fun insert(vararg drinks: Drink) {
        drinkDao.insert(*drinks)
    }

    suspend fun insert(vararg machines: Machine) {
        drinkDao.insert(*machines)
    }

    suspend fun deleteAll() {
        drinkDao.deleteAll()
    }
}