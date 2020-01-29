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
    private lateinit var authState: AuthState
    private lateinit var authService: AuthorizationService
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var littleDrinkFragment: DrinkFragment
    private lateinit var bigDrinkFragment: DrinkFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this)
            .get(MainActivityViewModel::class.java)

        viewModel.onErrorListener = object: MainActivityViewModel.OnErrorListener {
            override fun onError(errorCode: Int) { handleError(errorCode) }
        }

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

        sign_out_layout.setOnClickListener { confirmSignOut() }

        littleDrinkFragment = DrinkFragment(viewModel.littleDrink){ verifyCanDropDrink(it) }
        bigDrinkFragment = DrinkFragment(viewModel.bigDrink){ verifyCanDropDrink(it) }

        drink_srl.setOnRefreshListener { refresh() }

        setupTabs()

        authState = readAuthState()
        authService = AuthorizationService(this)
        getUserInfo()

        setSupportActionBar(toolbar)
        supportActionBar?.setIcon(R.drawable.ic_csh_logo_round)

        refresh()
    }

    private fun refresh(){
        drink_srl.isRefreshing = true
        refreshDrinkData()
        getDrinkCredits()
    }

    private fun refreshDrinkData(){
        authState.performActionWithFreshTokens(authService) { accessToken, _, ex ->
            ex?.let{
                Log.e("$TAG refresh", it.error ?: "there was an error retrieving tokens")
                return@performActionWithFreshTokens
            }

            accessToken?.let { viewModel.refreshDrinks(it) {
                drink_srl.isRefreshing = false
            } }
        }
    }

    private fun getUserInfo(){
        authState.performActionWithFreshTokens(authService) { accessToken, _, ex ->
            ex?.let{
                Log.e("TAG userInfo", it.error ?: "there was an error retrieving user info")
                return@performActionWithFreshTokens
            }
            accessToken?.let { viewModel.getUserInfo(it, authState.lastAuthorizationResponse!!.request.configuration.discoveryDoc!!.userinfoEndpoint!!) }
        }
    }

    private fun getDrinkCredits(){
        authState.performActionWithFreshTokens(authService) { accessToken, _, ex ->
            ex?.let{
                Log.e("TAG drinkCredits", it.error ?: "there was an error getting drink credits")
                return@performActionWithFreshTokens
            }
            accessToken?.let { viewModel.getDrinkCredits(it) }
        }
    }

    private fun verifyCanDropDrink(drink: Drink){
        if (!drink_srl.isRefreshing) {
            if (viewModel.credits.value ?: 0 < drink.cost) {
                alertNotEnoughCredits()
            } else {
                confirmDropDrink(drink)
            }
        }
    }

    private fun confirmDropDrink(drink: Drink){
        alert("Are you sure you want to drop a ${drink.name} for ${drink.cost} credits?") {
            yesButton { dropDrink(drink) }
            noButton {  }
        }.show()
    }

    private fun dropDrink(drink: Drink) {
        Log.i(TAG, "Dropping $drink")
        authState.performActionWithFreshTokens(authService) {accessToken, _, ex ->
            ex?.let{
                Log.e("TAG dropDrink", it.error ?: "there was an error dropping $drink")
                return@performActionWithFreshTokens
            }
            viewModel.dropDrink(accessToken!!, drink){
                refreshDrinkData()
                getDrinkCredits()
            }
        }
    }

    private fun confirmSignOut(){
        alert("Are you sure you want to sign out?"){
            yesButton{ signOut() }
            noButton { }
        }.show()
    }

    private fun readAuthState(): AuthState = AuthState.jsonDeserialize(viewModel.getAuthString())


    private fun signOut() {
        if (viewModel.signOut()){
            val intent = Intent(this@MainActivity, SignInActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setProfileImage(){
        viewModel.useUserProfileDrawable() {drawable ->
            toolbar.menu.getItem(0).icon = drawable
            nav_profile_image.setImageDrawable(drawable.constantState?.newDrawable())
        }
    }

    private fun handleError(code: Int){
        drink_srl.isRefreshing = false
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

    private fun setupTabs(){
        pager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)
        tab_layout.setupWithViewPager(pager)
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

        viewModel.uid.observe(this, Observer { uid ->
            nav_view.menu.findItem(R.id.nav_username_view)?.title = "User: $uid"
            setProfileImage()
        })

        viewModel.credits.observe(this, Observer { credits ->
            nav_view.menu.findItem(R.id.nav_credits_view)?.title = "Credits: $credits"
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean{
        when (item.itemId) {
            R.id.action_profile -> drawer.openDrawer(GravityCompat.END)
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = 2
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> littleDrinkFragment
                else -> bigDrinkFragment
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return viewModel.machines.value!![position].displayName
        }
    }
}
