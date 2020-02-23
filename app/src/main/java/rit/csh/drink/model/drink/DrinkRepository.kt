package rit.csh.drink.model.drink

import rit.csh.drink.model.User

class DrinkRepository(private val drinkDao: DrinkDao) {

    val machines = drinkDao.getMachinesWithDrinks()

    val user = drinkDao.getCurrentUser()

    suspend fun insert(vararg drinks: Drink) {
        drinkDao.insert(*drinks)
    }

    suspend fun insert(vararg machines: Machine) {
        drinkDao.insert(*machines)
    }

    suspend fun insert(user: User) {
        drinkDao.insert(user)
    }

    suspend fun deleteAll() {
        drinkDao.deleteAll()
    }
}