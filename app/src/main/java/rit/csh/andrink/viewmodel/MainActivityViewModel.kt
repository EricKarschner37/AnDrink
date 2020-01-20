package rit.csh.andrink.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import rit.csh.andrink.model.Drink
import rit.csh.andrink.model.DrinkRepository
import rit.csh.andrink.model.DrinkRoomDatabase
import rit.csh.andrink.model.NetworkManager

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DrinkRepository

    val TAG = "MainActivityViewModel"
    val bigDrinks: LiveData<List<Drink>>
    val littleDrinks: LiveData<List<Drink>>
    private val networkManager: NetworkManager

    init {
        val drinkDao = DrinkRoomDatabase.getDatabase(application).drinkDao()
        repository = DrinkRepository(drinkDao)
        bigDrinks = repository.bigDrinks
        littleDrinks = repository.littleDrinks
        networkManager = NetworkManager(application)
    }

    fun refreshDrinks(token: String, onComplete: () -> Unit){
        networkManager.getDrinks(token) { drinks ->
            GlobalScope.launch{
                repository.deleteAll()
            }
            for (drink in drinks) {
                runBlocking{
                    repository.insert(drink)
                }
                onComplete.invoke()
            }
        }
    }

    fun getUserInfo(token: String, endPoint: Uri, onComplete: (String) -> Unit){
        networkManager.getUserInfo(token, endPoint, onComplete)
    }

    fun dropDrink(token: String, drink: Drink, onComplete: () -> Unit){
        networkManager.dropItem(token, drink, onComplete)
    }

    fun getDrinkCredits(token: String, uid: String, onComplete: (Int) -> Unit){
        networkManager.getDrinkCredits(token, uid, onComplete)
    }
}