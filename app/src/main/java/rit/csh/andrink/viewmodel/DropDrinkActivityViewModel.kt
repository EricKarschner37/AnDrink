package rit.csh.andrink.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import net.openid.appauth.AuthState
import rit.csh.andrink.model.Drink
import rit.csh.andrink.model.NetworkManager

class DropDrinkActivityViewModel(application: Application): AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val networkManager = NetworkManager.getInstance(application)

    fun getAuthState(): AuthState {
        return if (prefs.contains("stateJson")){
            val stateJson = prefs.getString("stateJson", "")!!
            AuthState.jsonDeserialize(stateJson)
        } else {
            AuthState()
        }
    }

    fun dropDrink(token: String, drink: Drink, onComplete: () -> Unit){
        networkManager.dropItem(token, drink, onComplete)
    }
}