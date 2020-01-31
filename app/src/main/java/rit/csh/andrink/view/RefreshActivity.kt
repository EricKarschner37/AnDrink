package rit.csh.andrink.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProviders
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import rit.csh.andrink.R
import rit.csh.andrink.viewmodel.RefreshActivityViewModel

class RefreshActivity : AppCompatActivity() {

    lateinit var viewModel: RefreshActivityViewModel
    lateinit var authState: AuthState
    lateinit var authService: AuthorizationService
    private val TAG = "RefreshActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refresh)

        viewModel = ViewModelProviders.of(this).get(RefreshActivityViewModel::class.java)
        authState = viewModel.getAuthState()
        authService = AuthorizationService(this)

        retrieveMachineData()
        retrieveUserInfo()
    }

    private fun retrieveMachineData(){
        authState.performActionWithFreshTokens(authService) { accessToken, _, ex ->
            ex?.let{
                Log.e(TAG, it.error ?: "there was an error retrieving tokens")
                return@performActionWithFreshTokens
            }

            accessToken?.let {
                viewModel.getMachineData(it){
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    authService.dispose()
                    finish()
                }
            }
        }
    }

    private fun retrieveUserInfo(){
        authState.performActionWithFreshTokens(authService) { accessToken, _, ex ->
            ex?.let{
                Log.e(TAG, it.error ?: "there was an error retrieving tokens")
                return@performActionWithFreshTokens
            }

            accessToken?.let{
                viewModel.retrieveUserInfo(it, authState.lastAuthorizationResponse!!.request.configuration.discoveryDoc!!.userinfoEndpoint!!)
            }
        }
    }

    override fun onPause() {
        authService.dispose()
        super.onPause()
    }
}
