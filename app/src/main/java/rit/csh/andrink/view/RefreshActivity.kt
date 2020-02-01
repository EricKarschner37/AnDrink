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
    private val TAG = "RefreshActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refresh)

        viewModel = ViewModelProviders.of(this).get(RefreshActivityViewModel::class.java)

        retrieveMachineData()
        retrieveUserInfo()
    }

    private fun retrieveMachineData(){
        viewModel.getMachineData {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun retrieveUserInfo(){
        viewModel.retrieveUserInfo()
    }

    override fun onPause() {
        super.onPause()
    }
}
