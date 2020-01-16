package rit.csh.andrink.view

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import rit.csh.andrink.R

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"
    lateinit var authState: AuthState
    lateinit var authService: AuthorizationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        authState = readAuthState()
        authService = AuthorizationService(this)

        authState.performActionWithFreshTokens(authService) { accessToken, idToken, ex ->
            ex?.let{
                Log.e(TAG, it.error ?: "no log message available")
                return@performActionWithFreshTokens
            }
        }
    }

    private fun readAuthState(): AuthState {
        val authPrefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val stateJson = authPrefs.getString("stateJson", null)
        stateJson?.let {
            return AuthState.jsonDeserialize(stateJson)
        }
        return AuthState()
    }
}
