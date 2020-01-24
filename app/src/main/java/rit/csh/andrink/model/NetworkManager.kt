package rit.csh.andrink.model

import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class NetworkManager(context: Context) {
    val TAG = "NetworkManager"
    val baseUrl = "https://drink.csh.rit.edu"
    val queue = Volley.newRequestQueue(context)

    fun getDrinks(token: String, onComplete: (List<Drink>) -> Unit){
        val url = "$baseUrl/drinks"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener { response ->
                Log.i(TAG, response.toString())
                val drinks = parseJsonToDrinks(response)
                onComplete(drinks)
            },
            Response.ErrorListener { error ->
                Log.e(TAG, error.message ?: "There was an error retrieving the drinks")
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = hashMapOf<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }

        queue.add(jsonObjectRequest)
    }

    fun dropItem(token: String, drink: Drink, onComplete: () -> Unit) {
        val url = "$baseUrl/drinks/drop"

        val jsonBody = JSONObject()
        jsonBody.put("machine", drink.machine)
        jsonBody.put("slot", drink.slot)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            jsonBody,
            Response.Listener { response ->
                onComplete.invoke()
            },
            Response.ErrorListener {
                Log.e(TAG, it.toString())
            }

        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = hashMapOf<String, String>()
                headers["Authorization"] = "Bearer $token"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        queue.add(jsonObjectRequest)
    }

    fun getDrinkCredits(token: String, uid: String, onComplete: (Int) -> Unit) {
        val url = "$baseUrl/users/credits?uid=$uid"
        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener{ response ->
                onComplete.invoke(response.getJSONObject("user").getInt("drinkBalance"))
            },
            Response.ErrorListener {
                Log.e(TAG, it.toString())
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = hashMapOf<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }
        queue.add(jsonObjectRequest)
    }

    fun getUserInfo(token: String, endPoint: Uri, onComplete: (String) -> Unit) {
        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            endPoint.toString(),
            null,
            Response.Listener{ response ->
                Log.i(TAG, response.getString("preferred_username"))
                onComplete.invoke(response.getString("preferred_username"))
            },
            Response.ErrorListener { error ->

            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = hashMapOf<String, String>()
                headers["Authorization"] = "Bearer $token"
                return headers
            }
        }
        queue.add(jsonObjectRequest)
    }
}