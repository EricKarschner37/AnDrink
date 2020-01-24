package rit.csh.andrink.viewmodel

import android.app.Application
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.*
import rit.csh.andrink.model.*

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val drinkRepository: DrinkRepository
    private val profileImageRepository: ProfileImageRepository

    val TAG = "MainActivityViewModel"
    val bigDrinks: LiveData<List<Drink>>
    val littleDrinks: LiveData<List<Drink>>
    private val networkManager: NetworkManager

    init {
        val drinkDao = DrinkRoomDatabase.getDatabase(application).drinkDao()
        drinkRepository = DrinkRepository(drinkDao)
        profileImageRepository = ProfileImageRepository(application)
        bigDrinks = drinkRepository.bigDrinks
        littleDrinks = drinkRepository.littleDrinks
        networkManager = NetworkManager(application)
    }

    fun refreshDrinks(token: String, onComplete: () -> Unit){
        networkManager.getDrinks(token) { drinks ->
            runBlocking{
                drinkRepository.deleteAll()
            }
            for (drink in drinks) {
                runBlocking{
                    drinkRepository.insert(drink)
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

    fun useUserProfileDrawable(uid: String, useDrawable: (Drawable) -> Unit){
        profileImageRepository.useUserIconDrawable(uid, useDrawable)
    }
}