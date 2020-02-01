package rit.csh.andrink.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_drop_drink.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import rit.csh.andrink.R
import rit.csh.andrink.model.Drink
import rit.csh.andrink.viewmodel.DropDrinkActivityViewModel

class DropDrinkActivity : AppCompatActivity() {

    private val TAG = "DropDrinkActivity"
    private lateinit var viewModel: DropDrinkActivityViewModel
    private lateinit var drink: Drink

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drop_drink)

        intent.getParcelableExtra<Drink>("drink")?.let{
            drink = it
        }

        drop_drink_tv.text = "Dropping your ${drink.name}..."

        viewModel = ViewModelProviders.of(this).get(DropDrinkActivityViewModel::class.java)

        dropDrink(drink)
    }

    private fun dropDrink(drink: Drink) {
        Log.i(TAG, "Dropping $drink")

        viewModel.dropDrink(drink) {
            dropSuccessful(drink)
        }
    }

    private fun dropSuccessful(drink: Drink){
        drop_progress.visibility = View.GONE
        drop_drink_tv.text = "${drink.name} successfully dropped!"
        startRefresh()
    }

    private fun startRefresh(){
        val intent = Intent(this, RefreshActivity::class.java)
        startActivity(intent)
        finish()
    }
}
