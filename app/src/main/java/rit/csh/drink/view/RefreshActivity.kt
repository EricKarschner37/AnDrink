package rit.csh.drink.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import rit.csh.drink.R
import rit.csh.drink.model.Event
import rit.csh.drink.viewmodel.RefreshActivityViewModel

class RefreshActivity : AppCompatActivity() {

    lateinit var viewModel: RefreshActivityViewModel
    private val TAG = "RefreshActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refresh)

        viewModel = ViewModelProviders.of(this).get(RefreshActivityViewModel::class.java)
        viewModel.eventAlert.event.observe(this, Observer {
            when (it){
                Event.REFRESH_END -> {
                    launchMainActivity()
                    viewModel.eventAlert.complete()
                }
            }
        })

        retrieveMachineData()
        retrieveUserInfo()
    }

    private fun retrieveMachineData(){
        viewModel.getMachineData()
    }

    private fun launchMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun retrieveUserInfo(){
        viewModel.retrieveUserInfo()
    }

    override fun onBackPressed() {
        viewModel.cancelRefresh()
        super.onBackPressed()
    }
}
