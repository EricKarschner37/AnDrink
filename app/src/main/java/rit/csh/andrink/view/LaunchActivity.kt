package rit.csh.andrink.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import rit.csh.andrink.R
import rit.csh.andrink.viewmodel.LaunchActivityViewModel

class LaunchActivity : AppCompatActivity() {

    lateinit var viewModel: LaunchActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        viewModel = ViewModelProviders.of(this).get(LaunchActivityViewModel::class.java)

        val intent = if (viewModel.userIsAuthorized()){
            Intent(this, RefreshActivity::class.java)
        } else {
            Intent(this, SignInActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}
