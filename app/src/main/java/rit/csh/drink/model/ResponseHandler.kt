package rit.csh.drink.model

import com.github.kittinunf.fuel.core.FuelError
import org.json.JSONObject

abstract class ResponseHandler {
    abstract fun onSuccess(output: JSONObject)
    abstract fun onFailure(error: FuelError)
}