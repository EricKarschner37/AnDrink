package rit.csh.andrink.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import net.openid.appauth.AuthState
import rit.csh.andrink.model.AuthRequestManager
import rit.csh.andrink.model.Drink
import rit.csh.andrink.model.NetworkManager

class DropDrinkActivityViewModel(application: Application): AndroidViewModel(application) {

    private val networkManager = NetworkManager.getInstance(application)
    private val authManager = AuthRequestManager.getInstance(application)

    fun dropDrink(drink: Drink, onComplete: () -> Unit){
        authManager.makeRequest { token ->
            networkManager.dropItem(token, drink) {
                Log.i("DropVM", "drop drink success")
                onComplete.invoke()
            }
        }
    }
}