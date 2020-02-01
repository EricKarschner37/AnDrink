package rit.csh.andrink.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header.*
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.yesButton
import rit.csh.andrink.R
import rit.csh.andrink.model.Drink
import rit.csh.andrink.viewmodel.MainActivityViewModel

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var viewModel: MainActivityViewModel
    private val machineFragments = mutableListOf<DrinkFragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this)
            .get(MainActivityViewModel::class.java)

        nav_view.setNavigationItemSelectedListener { item ->
            when (item.itemId){
                R.id.nav_refresh -> {
                    refresh()
                    drawer.closeDrawers()
                    true
                }
                else -> true
            }
        }

        setupFragments()

        viewModel.machinesWithDrinks.observe(this, Observer { machinesWithDrinks ->
            setupFragments()
        })

        sign_out_layout.setOnClickListener { confirmSignOut() }

        setSupportActionBar(toolbar)
        supportActionBar?.setIcon(R.drawable.ic_csh_logo_round)
    }

    private fun refresh(){
        val intent = Intent(this, RefreshActivity::class.java)
        startActivity(intent)
    }

    private fun verifyCanDropDrink(drink: Drink){
        if (viewModel.credits < drink.cost) {
            alertNotEnoughCredits()
        } else {
            confirmDropDrink(drink)
        }
    }

    private fun confirmDropDrink(drink: Drink){
        alert("Are you sure you want to drop a ${drink.name} for ${drink.cost} credits?") {
            yesButton { dropDrink(drink) }
            noButton {  }
        }.show()
    }

    private fun dropDrink(drink: Drink){
        val intent = Intent(this, DropDrinkActivity::class.java)
        intent.putExtra("drink", drink)
        startActivity(intent)
        finish()
    }

    private fun confirmSignOut(){
        alert("Are you sure you want to sign out?"){
            yesButton{ signOut() }
            noButton { }
        }.show()
    }

    private fun signOut() {
        if (viewModel.signOut()){
            val intent = Intent(this@MainActivity, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setProfileImage(){
        viewModel.useUserProfileDrawable() {drawable ->
            toolbar.menu.getItem(0).icon = drawable
            nav_profile_image.setImageDrawable(drawable.constantState?.newDrawable())
        }
    }

    private fun handleError(code: Int){
        when (code){
            402 -> alertNotEnoughCredits()
            else -> Log.e(TAG, "Something went wrong. Error code $code")
        }
    }

    private fun alertNotEnoughCredits(){
        alert("You don't have enough credits for that"){
            okButton {  }
        }.show()
    }

    private fun setupFragments(){
        viewModel.machinesWithDrinks.value?.let { machinesWithDrinks ->
            for (machineWithDrinks in machinesWithDrinks){
                val fragment = DrinkFragment(machineWithDrinks){ verifyCanDropDrink(it) }
                machineFragments.add(fragment)
            }
            setupTabs()
        }
    }

    private fun setupTabs(){
        pager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)
        tab_layout.setupWithViewPager(pager)

        viewModel.machinesWithDrinks.value?.let{
            for (index in it.indices){
                Log.i(TAG, "$index: ${it[index]}")
                tab_layout.getTabAt(index)?.setIcon(it[index].machine.getDrawableForStatus())
            }
        }
    }

    override fun onBackPressed() {
        if (pager.currentItem == 0){
            super.onBackPressed()
        } else {
            pager.currentItem--
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)

        setProfileImage()

        nav_view.menu.findItem(R.id.nav_credits_view)?.title = "Credits: ${viewModel.credits}"
        nav_view.menu.findItem(R.id.nav_username_view)?.title = "User: ${viewModel.uid}"

        setProfileImage()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean{
        when (item.itemId) {
            R.id.action_profile -> drawer.openDrawer(GravityCompat.END)
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = machineFragments.size
        override fun getItem(position: Int): Fragment {
            return machineFragments[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return machineFragments[position].pageTitle
        }
    }
}
