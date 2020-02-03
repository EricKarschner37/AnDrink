package rit.csh.andrink.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.kittinunf.fuel.core.FuelError
import rit.csh.andrink.model.*
import kotlinx.coroutines.*
import net.openid.appauth.AuthState
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

class RefreshActivityViewModel(application: Application): AndroidViewModel(application) {

    private val TAG = "RefreshVM"
    private val prefs = application.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val networkManager = NetworkManager.getInstance(application)
    private val authManager = AuthRequestManager.getInstance(application)
    private val drinkRepository: DrinkRepository

    init {
        val drinkDao = DrinkRoomDatabase.getDatabase(application).drinkDao()
        drinkRepository = DrinkRepository(drinkDao)
    }

    fun retrieveUserInfo(){
        val creditsHandler = object: ResponseHandler<Int>() {
            override fun onSuccess(output: Int) {
                setCredits(output)
            }

            override fun onFailure(error: FuelError) {
                Log.e(TAG, error.message ?: "Something went wrong retrieving the user's credits")
            }

        }
        authManager.makeRequest { token ->
            val uidHandler = object: ResponseHandler<String>() {
                override fun onSuccess(output: String) {
                    setUid(output)
                    networkManager.getDrinkCredits(token, output, creditsHandler)
                }

                override fun onFailure(error: FuelError) {
                    Log.e(TAG, error.message ?: "Something went wrong retrieving the user id")
                }
            }
            networkManager.getUserInfo(token, authManager.getUserInfoEndpoint(), uidHandler)
        }
    }

    fun getMachineData(onComplete: () -> Unit){
        val handler = object: ResponseHandler<JSONObject>() {

            override fun onSuccess(output: JSONObject) {
                val machineDrinksPair = parseMachineJson(output)
                viewModelScope.launch{
                    writeToDatabase(machineDrinksPair.first.requireNoNulls(), machineDrinksPair.second.toTypedArray())
                }
                onComplete.invoke()
            }

            override fun onFailure(error: FuelError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        authManager.makeRequest { token ->
            networkManager.getDrinks(token, handler)
        }
    }

    fun cancelRefresh(){
        authManager.flushRequests()
    }

    private fun parseMachineJson(input: JSONObject): Pair<Array<Machine?>, ArrayList<Drink>> {
        val machineJsonArray = input.getJSONArray("machines")
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
        return Pair(machines, drinks)
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