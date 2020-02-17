package rit.csh.drink.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.kittinunf.fuel.core.FuelError
import rit.csh.drink.model.*
import kotlinx.coroutines.*
import org.json.JSONObject
import rit.csh.drink.model.drink.Drink
import rit.csh.drink.model.drink.DrinkRepository
import rit.csh.drink.model.drink.DrinkRoomDatabase
import rit.csh.drink.model.drink.Machine

class RefreshActivityViewModel(application: Application): AndroidViewModel(application) {

    private val TAG = "RefreshVM"
    val eventAlert = EventAlert()
    private val networkManager = NetworkManager.getInstance(application)
    private val authManager: AuthRequestManager
    private val drinkRepository: DrinkRepository

    init {
        val drinkDao = DrinkRoomDatabase.getDatabase(application).drinkDao()
        drinkRepository = DrinkRepository(drinkDao)
        authManager = AuthRequestManager(application) {
            eventAlert.setEvent(Event.ERROR)
        }
    }

    fun retrieveUserInfo(){
        lateinit var uid: String
        val creditsHandler = object: ResponseHandler() {
            override fun onSuccess(output: JSONObject) {
                val user = User(uid, output.parseCreditsFromJson(), true)
                viewModelScope.launch{
                    writeUser(user)
                }
            }

            override fun onFailure(error: FuelError) {
                Log.e(TAG, error.message ?: "Something went wrong retrieving the user's credits")
            }
        }

        authManager.makeRequest { token ->
            val uidHandler = object: ResponseHandler() {
                override fun onSuccess(output: JSONObject) {
                    uid = output.parseUidFromJson()
                    networkManager.getDrinkCredits(token, uid, creditsHandler)
                }

                override fun onFailure(error: FuelError) {
                    Log.e(TAG, error.message ?: "Something went wrong retrieving the user id")
                }
            }
            networkManager.getUserInfo(token, authManager.getUserInfoEndpoint(), uidHandler)
        }
    }

    fun getMachineData(){
        val handler = object: ResponseHandler() {

            override fun onSuccess(output: JSONObject) {
                val machineDrinksPair = parseMachineJson(output)
                viewModelScope.launch{
                    writeToDatabase(machineDrinksPair.first.requireNoNulls(), machineDrinksPair.second.toTypedArray())
                }
                eventAlert.setEvent(Event.REFRESH_END)
            }

            override fun onFailure(error: FuelError) {
                Log.i(TAG, error.message ?: "Something went wrong retrieving stock information")
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

    private fun JSONObject.parseCreditsFromJson() =
        getJSONObject("user").getInt("drinkBalance")

    private fun JSONObject.parseUidFromJson() =
        getString("preferred_username")

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

    private suspend fun writeUser(user: User) {
        drinkRepository.insert(user)
    }
}