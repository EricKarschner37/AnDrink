package rit.csh.drink.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.github.kittinunf.fuel.core.FuelError
import org.json.JSONObject
import rit.csh.drink.model.*
import rit.csh.drink.model.drink.Drink

class DropDrinkActivityViewModel(application: Application): AndroidViewModel(application) {

    val TAG = "DropDrinkVM"
    val eventAlert = EventAlert()
    private val networkManager = NetworkManager.getInstance(application)
    private val authManager: AuthRequestManager = AuthRequestManager(application){
        eventAlert.setEvent(Event.ERROR)
    }

    fun dropDrink(drink: Drink){
        val handler = object: ResponseHandler() {
            override fun onSuccess(output: JSONObject) {
                Log.i(TAG, "Drink successfully dropped")
                eventAlert.setEvent(Event.DROP_DRINK_END)
            }

            override fun onFailure(error: FuelError) {
                Log.i(TAG, error.message ?: "Something went wrong dropping $drink")
                eventAlert.setEvent(Event.DROP_DRINK_END)
            }
        }
        authManager.makeRequest { token ->
            networkManager.dropItem(token, drink, handler)
        }
    }
}