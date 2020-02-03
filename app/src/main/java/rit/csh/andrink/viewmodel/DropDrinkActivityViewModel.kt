package rit.csh.andrink.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.github.kittinunf.fuel.core.FuelError
import net.openid.appauth.AuthState
import rit.csh.andrink.model.AuthRequestManager
import rit.csh.andrink.model.Drink
import rit.csh.andrink.model.NetworkManager
import rit.csh.andrink.model.ResponseHandler

class DropDrinkActivityViewModel(application: Application): AndroidViewModel(application) {

    private val networkManager = NetworkManager.getInstance(application)
    private val authManager = AuthRequestManager.getInstance(application)

    fun dropDrink(drink: Drink, handler: ResponseHandler<String>){
        authManager.makeRequest { token ->
            networkManager.dropItem(token, drink, handler)
        }
    }
}