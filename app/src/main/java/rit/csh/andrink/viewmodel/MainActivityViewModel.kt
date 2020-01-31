package rit.csh.andrink.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import net.openid.appauth.AuthState
import rit.csh.andrink.model.*

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val drinkRepository: DrinkRepository
    private val profileImageRepository: ProfileImageRepository
    private val prefs: SharedPreferences

    val TAG = "MainActivityViewModel"
    val machinesWithDrinks: LiveData<List<MachineWithDrinks>>
    var uid: String
    var credits: Int
    private val networkManager: NetworkManager

    init {
        val drinkDao = DrinkRoomDatabase.getDatabase(application).drinkDao()
        drinkRepository = DrinkRepository(drinkDao)
        profileImageRepository = ProfileImageRepository(application)
        prefs = application.getSharedPreferences("auth", Context.MODE_PRIVATE)

        machinesWithDrinks = drinkRepository.machines

        uid = prefs.getString("uid", "")!!
        credits = prefs.getInt("credits", 0)
        networkManager = NetworkManager(application)
    }

    fun getAuthState(): AuthState {
        return AuthState.jsonDeserialize(prefs.getString("stateJson", "")!!)
    }

    fun useUserProfileDrawable(useDrawable: (Drawable) -> Unit){
        profileImageRepository.useUserIconDrawable(uid, useDrawable)
    }

    fun signOut(): Boolean {
        profileImageRepository.deleteUserIcon(uid)

        return prefs.edit()
            .clear()
            .commit()
    }
}