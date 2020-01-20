package rit.csh.andrink.model

import androidx.lifecycle.LiveData

class DrinkRepository(private val drinkDao: DrinkDao) {
    val bigDrinks = drinkDao.getDrinksForMachine("bigdrink")
    val littleDrinks = drinkDao.getDrinksForMachine("littledrink")

    suspend fun insert(drink: Drink) {
        drinkDao.insert(drink)
    }

    suspend fun deleteAll() {
        drinkDao.deleteAll()
    }
}