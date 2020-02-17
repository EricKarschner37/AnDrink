package rit.csh.drink.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import rit.csh.drink.model.*
import rit.csh.drink.model.drink.DrinkRepository
import rit.csh.drink.model.drink.DrinkRoomDatabase
import rit.csh.drink.model.drink.MachineWithDrinks
import rit.csh.drink.model.user.ProfileImageRepository

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val drinkRepository: DrinkRepository
    private val profileImageRepository: ProfileImageRepository
    private val prefs: SharedPreferences

    val TAG = "MainActivityViewModel"
    val machinesWithDrinks: LiveData<List<MachineWithDrinks>>
    val user: LiveData<User>

    init {
        val drinkDao = DrinkRoomDatabase.getDatabase(application).drinkDao()
        drinkRepository = DrinkRepository(drinkDao)
        profileImageRepository =
            ProfileImageRepository(application)
        prefs = application.getSharedPreferences("auth", Context.MODE_PRIVATE)

        machinesWithDrinks = drinkRepository.machines
        user = drinkRepository.user
    }

    fun useUserProfileDrawable(useDrawable: (Drawable) -> Unit){
        user.value?.let{
            profileImageRepository.useUserIconDrawable(user.value!!.uid, useDrawable)
        }
    }

    fun signOut() = prefs.edit()
        .clear()
        .commit()
}