package rit.csh.drink.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri
import net.openid.appauth.*
import rit.csh.drink.R

class SignInActivity : AppCompatActivity() {

    val RC_AUTH = 1000
    val TAG = "SignInActivity"
    lateinit var authState: AuthState
    lateinit var authService: AuthorizationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        authState = readAuthState()
        authService = AuthorizationService(this)

        if (authState.isAuthorized) {
            launchRefreshActivity()
        } else {
            authenticate()
        }
    }

    private fun authenticate(){
        val issuerUri = Uri.parse("https://sso.csh.rit.edu/auth/realms/csh")
        AuthorizationServiceConfiguration.fetchFromIssuer(issuerUri) { result, ex ->
            ex?.let {
                Log.w(TAG, "Failed to retrieve configuration", ex)
            }

            val req = AuthorizationRequest.Builder(
                result!!,
                "AnDrink",
                ResponseTypeValues.CODE,
                "drink://redirect".toUri()
            ).setScopes(
                "openid",
                "offline_access",
                "profile",
                "drink_balance"
            ).setPrompt("login").build()

            val authIntent = authService.getAuthorizationRequestIntent(req)
            startActivityForResult(authIntent, RC_AUTH)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_AUTH) {
            handleAuthorizationResponse(data!!)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleAuthorizationResponse(intent: Intent){
        val response = AuthorizationResponse.fromIntent(intent)
        val error = AuthorizationException.fromIntent(intent)

        authState.update(response, error)
        Log.i(TAG, authState.jsonSerializeString())

        authService.performTokenRequest(response!!.createTokenExchangeRequest()) { resp, ex ->
            authState.update(resp, ex)
            writeAuthState(authState)
            launchRefreshActivity()
        }
    }

    private fun readAuthState(): AuthState {
        val authPrefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val stateJson = authPrefs.getString("stateJson", null)
        Log.i(TAG, stateJson ?: "there is no previous login data")
        stateJson?.let {
            return AuthState.jsonDeserialize(stateJson)
        }
        return AuthState()
    }

    private fun writeAuthState(state: AuthState) {
        val authPrefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        authPrefs.edit()
            .putString("stateJson", state.jsonSerializeString())
            .apply()
    }

    private fun launchRefreshActivity(){
        val intent = Intent(this, RefreshActivity::class.java)
        startActivity(intent)
        authService.dispose()
        finish()
    }
}
