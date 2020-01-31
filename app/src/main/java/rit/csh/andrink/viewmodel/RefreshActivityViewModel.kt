package rit.csh.andrink.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import rit.csh.andrink.model.*
import kotlinx.coroutines.*
import net.openid.appauth.AuthState
import kotlin.coroutines.CoroutineContext

class RefreshActivityViewModel(application: Application): AndroidViewModel(application) {

    private val TAG = "RefreshVM"
    private val prefs = application.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val networkManager = NetworkManager(application)
    private val drinkRepository: DrinkRepository

    init {
        val drinkDao = DrinkRoomDatabase.getDatabase(application).drinkDao()
        drinkRepository = DrinkRepository(drinkDao)
    }

    fun getAuthState(): AuthState {
        val stateJson = prefs.getString("stateJson", "")!!
        return AuthState.jsonDeserialize(stateJson)
    }

    fun retrieveUserInfo(token: String, endPoint: Uri){
        networkManager.getUserInfo(token, endPoint){uid ->
            setUid(uid)

            networkManager.getDrinkCredits(token, uid) {
                setCredits(it)
            }
        }
    }

    private fun setCredits(new: Int) {
        prefs.edit()
            .putInt("credits", new)
            .apply()
    }

    private fun setUid(new: String) {
        prefs.edit()
            .putString("uid", new)
            .apply()
    }

    fun getMachineData(token: String, onComplete: () -> Unit){
        networkManager.getDrinks(token) { response ->
            val machineJsonArray = response.getJSONArray("machines")
            val machines = arrayOfNulls<Machine>(machineJsonArray.length())
            val drinks = arrayListOf<Drink>()
            for (index in 0 until machineJsonArray.length()){
                val machineJson = machineJsonArray.getJSONObject(index)
                drinks.addAll(Drink.parseJsonToDrinks(machineJson.getJSONArray("slots"), machineJson.getString("name")))
                val machine = Machine(
                    machineJson.getString("name"),
                    machineJson.getString("display_name"),
                    machineJson.getBoolean("is_online")
                )
                machines[index] = machine
            }
            GlobalScope.launch {
                writeToDatabase(machines.requireNoNulls(), drinks.toTypedArray())
            }
            onComplete.invoke()
        }
    }

    private suspend fun writeToDatabase(machines: Array<Machine>, drinks: Array<Drink>){
        drinkRepository.deleteAll()
        GlobalScope.launch { writeMachines(machines) }
        GlobalScope.launch { writeDrinks(drinks)}
    }

    private suspend fun writeMachines(machines: Array<Machine>){
        Log.i("RefreshVM", machines.toString())
        drinkRepository.insert(*machines)
    }

    private suspend fun writeDrinks(drinks: Array<Drink>) {
        drinkRepository.insert(*drinks)
    }
}