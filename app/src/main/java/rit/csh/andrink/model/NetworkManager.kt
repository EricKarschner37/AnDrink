package rit.csh.andrink.model

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.ResultHandler
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.github.kittinunf.result.Result
import org.json.JSONObject

class NetworkManager private constructor(context: Context){
    private val TAG = "NetworkManager"
    private val baseUrl = "https://drink.csh.rit.edu"
    private val mainHandler = Handler(context.mainLooper)

    fun getDrinks(token: String, handler: ResponseHandler<JSONObject>): CancellableRequest{
        val url = "$baseUrl/drinks"

        Log.i(TAG, "getDrinks")

        return Fuel.get(url)
            .authentication()
            .bearer(token)
            .responseString { request, response, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        Log.e(TAG, ex.message ?: "There was an issue with retrieving the drinks")
                    }
                    is Result.Success -> {
                        val jsonResponse = JSONObject(String(response.data))
                        val runnable = Runnable {
                            handler.onSuccess(jsonResponse)
                        }
                        mainHandler.post(runnable)
                    }
                }
            }
    }

    fun dropItem(token: String, drink: Drink, handler: ResponseHandler<String>): CancellableRequest {
        val url = "$baseUrl/drinks/drop"

        val jsonBody = JSONObject()
        jsonBody.put("machine", drink.machine)
        jsonBody.put("slot", drink.slot)

        return Fuel.post(url)
            .jsonBody(jsonBody.toString())
            .authentication()
            .bearer(token)
            .responseString { request, response, result ->
                when (result){
                    is Result.Failure-> {
                        val ex = result.getException()
                        handler.onFailure(ex)
                    }
                    is Result.Success -> {
                        val runnable = Runnable {
                            handler.onSuccess(result.value)
                        }
                        mainHandler.post(runnable)
                    }
                }
            }
    }

    fun getDrinkCredits(token: String, uid: String, handler: ResponseHandler<Int>): CancellableRequest {
        val url = "$baseUrl/users/credits?uid=$uid"

        return Fuel.get(url)
            .authentication()
            .bearer(token)
            .responseString { request, response, result ->
                when (result) {
                    is Result.Failure -> {
                        handler.onFailure(result.error)
                    }
                    is Result.Success -> {
                        val data = JSONObject(String(response.data))
                        val credits = data.getJSONObject("user").getInt("drinkBalance")
                        val runnable = Runnable {
                            handler.onSuccess(credits)
                        }
                        mainHandler.post(runnable)
                    }
                }

            }
    }

    fun getUserInfo(token: String, endPoint: Uri, handler: ResponseHandler<String>): CancellableRequest {
        return Fuel.get(endPoint.toString())
            .authentication()
            .bearer(token)
            .responseString { request, response, result ->
                when (result){
                    is Result.Failure -> {
                        handler.onFailure(result.error)
                    }
                    is Result.Success -> {
                        val data = JSONObject(String(response.data))
                        val runnable = Runnable {
                            handler.onSuccess(data.getString("preferred_username"))
                        }
                        mainHandler.post(runnable)
                    }
                }
            }
    }

    companion object {
        @Volatile
        private var INSTANCE: NetworkManager? = null

        fun getInstance(context: Context): NetworkManager{
            if (INSTANCE != null) {
                return INSTANCE as NetworkManager
            }
            val instance = NetworkManager(context)
            INSTANCE = instance
            return instance
        }
    }
}