package rit.csh.andrink.model

import androidx.lifecycle.LiveData

class DrinkRepository(private val drinkDao: DrinkDao) {
    val allDrinks: LiveData<List<Drink>> = drinkDao.getAllDrinks()

    suspend fun insert(drink: Drink) {
        drinkDao.insert(drink)
    }
}