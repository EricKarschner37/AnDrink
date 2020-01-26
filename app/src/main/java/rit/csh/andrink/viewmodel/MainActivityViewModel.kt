package rit.csh.andrink.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.ContactsContract
import android.view.MenuItem
import android.widget.ImageView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import rit.csh.andrink.model.*

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val drinkRepository: DrinkRepository
    private val profileImageRepository: ProfileImageRepository
    private val prefs: SharedPreferences

    val TAG = "MainActivityViewModel"
    val bigDrinks: LiveData<List<Drink>>
    val littleDrinks: LiveData<List<Drink>>
    var uid = MutableLiveData<String>("")
    var credits = MutableLiveData(0)
    private val networkManager: NetworkManager

    init {
        val drinkDao = DrinkRoomDatabase.getDatabase(application).drinkDao()
        drinkRepository = DrinkRepository(drinkDao)
        profileImageRepository = ProfileImageRepository(application)
        prefs = application.getSharedPreferences("auth", Context.MODE_PRIVATE)
        bigDrinks = drinkRepository.bigDrinks
        littleDrinks = drinkRepository.littleDrinks
        uid = MutableLiveData(prefs.getString("uid", "")!!)
        credits = MutableLiveData(prefs.getInt("credits", 0))
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

    fun getUserInfo(token: String, endPoint: Uri){
        networkManager.getUserInfo(token, endPoint){newUid ->
            setUid(newUid)
        }
    }

    fun dropDrink(token: String, drink: Drink, onComplete: () -> Unit){
        networkManager.dropItem(token, drink, onComplete)
    }

    fun getDrinkCredits(token: String, uid: String){
        networkManager.getDrinkCredits(token, uid) { newCredits ->
            setCredits(newCredits)
        }
    }

    fun useUserProfileDrawable(uid: String, useDrawable: (Drawable) -> Unit){
        profileImageRepository.useUserIconDrawable(uid, useDrawable)
    }

    fun setUid(new: String) {
        uid.value = new
        prefs.edit()
            .putString("uid", new)
            .apply()
    }

    fun setCredits(new: Int) {
        credits.value = new
        prefs.edit()
            .putInt("credits", new)
            .apply()
    }
}