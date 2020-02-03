package rit.csh.andrink.model

import com.github.kittinunf.fuel.core.FuelError

abstract class ResponseHandler<T> {
    abstract fun onSuccess(output: T)
    abstract fun onFailure(error: FuelError)
}