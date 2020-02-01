package rit.csh.andrink.model

import android.content.Context
import android.net.Uri
import android.util.Log
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import java.net.URI

class AuthRequestManager private constructor(context: Context) {

    private val TAG = "AuthRequest"
    private val requests = ArrayList<CancellableRequest>()
    private val authService = AuthorizationService(context)
    private val authState: AuthState

    init {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val stateJson = prefs.getString("stateJson", "")!!
        authState = AuthState.jsonDeserialize(stateJson)
    }

    fun makeRequest(request: (String) -> CancellableRequest) {
        authState.performActionWithFreshTokens(authService) { accessToken, idToken, ex ->
            ex?.let {
                Log.e(TAG, it.error ?: "something went wrong getting tokens")
                return@performActionWithFreshTokens
            }

            accessToken?.let{
                requests.add(request.invoke(it))
            }
        }
    }

    fun flushRequests(){
        while (requests.isNotEmpty()) {
            requests.removeAt(0).cancel()
        }
    }

    fun getUserInfoEndpoint(): Uri = authState.lastAuthorizationResponse!!.request.configuration.discoveryDoc!!.userinfoEndpoint!!

    companion object {
        @Volatile
        private var INSTANCE: AuthRequestManager? = null

        fun getInstance(context: Context): AuthRequestManager {
            return if (INSTANCE != null) {
                INSTANCE!!
            } else {
                val instance = AuthRequestManager(context)
                INSTANCE = instance
                instance
            }
        }
    }
}