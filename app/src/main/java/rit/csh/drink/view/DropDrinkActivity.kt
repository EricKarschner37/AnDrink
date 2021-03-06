package rit.csh.drink.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_drop_drink.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import rit.csh.drink.R
import rit.csh.drink.model.drink.Drink
import rit.csh.drink.model.Event
import rit.csh.drink.viewmodel.DropDrinkActivityViewModel

class DropDrinkActivity : AppCompatActivity() {

    private val TAG = "DropDrinkActivity"
    private lateinit var viewModel: DropDrinkActivityViewModel
    private lateinit var drink: Drink

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drop_drink)

        viewModel = ViewModelProviders.of(this).get(DropDrinkActivityViewModel::class.java)

        intent.getParcelableExtra<Drink>("drink")?.let{
            drink = it
        }

        drop_drink_tv.text = "Dropping your ${drink.name}..."

        viewModel.eventAlert.event.observe(this, Observer {
            when (it){
                Event.DROP_DRINK_END -> {
                    dropSuccessful()
                    viewModel.eventAlert.complete()
                }
                Event.ERROR -> {
                    dropFailed()
                    viewModel.eventAlert.complete()
                }
            }
        })

        dropDrink()
    }

    private fun dropDrink() {
        Log.i(TAG, "Dropping $drink")
        viewModel.dropDrink(drink)
    }

    private fun dropSuccessful(){
        drop_progress.visibility = View.GONE
        drop_drink_tv.text = "${drink.name} successfully dropped!"
        GlobalScope.launch{
            Thread.sleep(2000)
            startRefresh()
        }
    }

    private fun dropFailed(){
        drop_progress.visibility = View.GONE
        drop_drink_tv.text = "Sorry, something went wrong while dropping ${drink.name}"
        GlobalScope.launch{
            Thread.sleep(2000)
            startRefresh()
        }
    }

    private fun startRefresh(){
        val intent = Intent(this, RefreshActivity::class.java)
        startActivity(intent)
        finish()
    }
}
