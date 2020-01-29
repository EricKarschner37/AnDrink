package rit.csh.andrink.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import rit.csh.andrink.model.*

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val drinkRepository: DrinkRepository
    private val profileImageRepository: ProfileImageRepository
    private val prefs: SharedPreferences

    val TAG = "MainActivityViewModel"
    val bigDrink: Machine
    val littleDrink: Machine
    var uid = MutableLiveData<String>("")
    var credits = MutableLiveData(0)
    private val networkManager: NetworkManager
    var onErrorListener = object: OnErrorListener{
        override fun onError(errorCode: Int) { Log.i(TAG, "OnErrorListener must be set in MainActivity") }
    }

    init {
        val drinkDao = DrinkRoomDatabase.getDatabase(application).drinkDao()
        drinkRepository = DrinkRepository(drinkDao)
        profileImageRepository = ProfileImageRepository(application)
        prefs = application.getSharedPreferences("auth", Context.MODE_PRIVATE)

        bigDrink = Machine(
            drinkRepository.bigDrinks,
            "bigdrink",
            "Big Drink",
            MutableLiveData(Status.UNKNOWN))

        littleDrink = Machine(
            drinkRepository.littleDrinks,
            "bigdrink",
            "Big Drink",
            MutableLiveData(Status.UNKNOWN))

        uid = MutableLiveData(prefs.getString("uid", "")!!)
        credits = MutableLiveData(prefs.getInt("credits", 0))
        networkManager = NetworkManager(application) { onErrorListener.onError(it) }
    }

    fun getAuthString(): String{
        return prefs.getString("stateJson", "")!!
    }

    fun refreshDrinks(token: String, onComplete: () -> Unit){
        networkManager.getDrinks(token) { drinks, bigOnline, littleOnline ->
            bigDrink.setStatus(bigOnline)
            littleDrink.setStatus(littleOnline)
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

    fun getDrinkCredits(token: String){
        networkManager.getDrinkCredits(token, uid.value ?: "") { newCredits ->
            setCredits(newCredits)
        }
    }

    fun useUserProfileDrawable(useDrawable: (Drawable) -> Unit){
        profileImageRepository.useUserIconDrawable(uid.value!!, useDrawable)
    }

    fun signOut(): Boolean {
        profileImageRepository.deleteUserIcon(uid.value!!)

        return prefs.edit()
            .clear()
            .commit()
    }

    private fun setUid(new: String) {
        uid.value = new
        prefs.edit()
            .putString("uid", new)
            .apply()
    }

    private fun setCredits(new: Int) {
        credits.value = new
        prefs.edit()
            .putInt("credits", new)
            .apply()
    }

    interface OnErrorListener{
        fun onError(errorCode: Int)
    }
}