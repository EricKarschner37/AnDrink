package rit.csh.andrink.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import rit.csh.andrink.model.Drink
import rit.csh.andrink.model.DrinkRepository
import rit.csh.andrink.model.DrinkRoomDatabase

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DrinkRepository

    val allDrinks: LiveData<List<Drink>>

    init {
        val drinkDao = DrinkRoomDatabase.getDatabase(application).drinkDao()
        repository = DrinkRepository(drinkDao)
        allDrinks = repository.allDrinks
    }

    fun insert(drink: Drink) = viewModelScope.launch {
        repository.insert(drink)
    }
}