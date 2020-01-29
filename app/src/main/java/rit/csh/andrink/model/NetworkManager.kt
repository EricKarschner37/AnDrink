package rit.csh.andrink.model

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import org.json.JSONObject

class NetworkManager(context: Context, private val onError: (Int) -> Unit) {
    private val TAG = "NetworkManager"
    private val baseUrl = "https://drink.csh.rit.edu"
    private val mainHandler = Handler(context.mainLooper)

    private fun verifyDeviceIsConnected(){

    }

    fun getDrinks(token: String, onComplete: (List<Drink>, Boolean, Boolean) -> Unit){
        val url = "$baseUrl/drinks"

        Log.i(TAG, "getDrinks")

        Fuel.get(url)
            .authentication()
            .bearer(token)
            .responseString { request, response, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        Log.e(TAG, ex.message ?: "There was an issue with retrieving the drinks")
                    }
                    is Result.Success -> {
                        Log.i(TAG, String(response.data))
                        val jsonResponse = JSONObject(String(response.data))
                        val drinks = parseJsonToDrinks(jsonResponse)
                        jsonResponse.getJSONArray("machines").let{
                            if (it.getJSONObject(0).getString("name") == "bigdrink"){
                                val runnable = Runnable {
                                    onComplete.invoke(drinks,
                                        it.getJSONObject(0).getBoolean("is_online"),
                                        it.getJSONObject(1).getBoolean("is_online"))
                                }
                                mainHandler.post(runnable)
                            } else {
                                val runnable = {
                                    onComplete.invoke(drinks,
                                        it.getJSONObject(1).getBoolean("is_online"),
                                        it.getJSONObject(0).getBoolean("is_online"))
                                }
                                mainHandler.post(runnable)
                            }
                        }
                    }
                }
            }
    }

    fun dropItem(token: String, drink: Drink, onComplete: () -> Unit) {
        val url = "$baseUrl/drinks/drop"

        val jsonBody = JSONObject()
        jsonBody.put("machine", drink.machine)
        jsonBody.put("slot", drink.slot)

        Fuel.post(url)
            .jsonBody(jsonBody.toString())
            .authentication()
            .bearer(token)
            .responseString { request, response, result ->
                when (result){
                    is Result.Failure-> {
                        val ex = result.getException()
                        val errorCode = JSONObject(String(ex.errorData)).getInt("errorCode")
                        onError.invoke(errorCode)
                        Log.e(TAG, String(ex.errorData))
                    }
                    is Result.Success -> {
                        onComplete.invoke()
                    }
                }
            }
    }

    fun getDrinkCredits(token: String, uid: String, onComplete: (Int) -> Unit) {
        val url = "$baseUrl/users/credits?uid=$uid"

        Fuel.get(url)
            .authentication()
            .bearer(token)
            .responseString { request, response, result ->
                when (result) {
                    is Result.Failure -> {
                        Log.e(TAG, result.error.message ?: "Something went wrong while" +
                        "retrieving drink credits")
                    }
                    is Result.Success -> {
                        val data = JSONObject(String(response.data))
                        val credits = data.getJSONObject("user").getInt("drinkBalance")
                        val runnable = Runnable {
                            onComplete.invoke(credits)
                        }
                        mainHandler.post(runnable)
                    }
                }

            }
    }

    fun getUserInfo(token: String, endPoint: Uri, onComplete: (String) -> Unit) {

        Fuel.get(endPoint.toString())
            .authentication()
            .bearer(token)
            .responseString { request, response, result ->
                when (result){
                    is Result.Failure -> {
                        Log.e(TAG, result.error.message ?: "Something went wrong retrieving the username")
                    }
                    is Result.Success -> {
                        val data = JSONObject(String(response.data))
                        val runnable = Runnable {
                            onComplete.invoke(data.getString("preferred_username"))
                        }
                        mainHandler.post(runnable)
                    }
                }
            }
    }
}