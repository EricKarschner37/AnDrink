package rit.csh.andrink.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_launch.*
import rit.csh.andrink.R
import rit.csh.andrink.viewmodel.LaunchActivityViewModel

class LaunchActivity : AppCompatActivity() {

    lateinit var viewModel: LaunchActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        viewModel = ViewModelProviders.of(this).get(LaunchActivityViewModel::class.java)

        if (viewModel.userIsAuthorized()) launchRefresh()
        sign_in_btn.setOnClickListener { launchSignIn() }
    }

    private fun launchRefresh(){
        val intent = Intent(this, RefreshActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun launchSignIn(){
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}
