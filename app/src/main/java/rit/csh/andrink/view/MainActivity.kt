package rit.csh.andrink.view

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProviders
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.activity_main.*
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import rit.csh.andrink.R
import rit.csh.andrink.model.Drink
import rit.csh.andrink.viewmodel.MainActivityViewModel
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var authState: AuthState
    private lateinit var authService: AuthorizationService
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var littleDrinkFragment: LittleDrinkFragment
    private lateinit var bigDrinkFragment: BigDrinkFragment
    private lateinit var uid: String
    private lateinit var image_url: String
    private lateinit var prefs: SharedPreferences
    private var credits = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        uid = prefs.getString("uid", "")!!
        credits = prefs.getInt("credits", 0)

        viewModel = ViewModelProviders.of(this)
            .get(MainActivityViewModel::class.java)

        nav_view.setNavigationItemSelectedListener {

            when (it.itemId) {
                R.id.sign_out_menu -> Log.i(TAG, "sign out")
            }

            true
        }

        littleDrinkFragment = LittleDrinkFragment{ confirmDropDrink(it) }
        bigDrinkFragment = BigDrinkFragment{ confirmDropDrink(it) }

        drink_srl.setOnRefreshListener {
            refreshDrinkData()
            getDrinkCredits()
        }

        pager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)
        tab_layout.setupWithViewPager(pager)

        authState = readAuthState()
        authService = AuthorizationService(this)

        drink_srl.isRefreshing = true
        refreshDrinkData()
        getDrinkCredits()
        getUserInfo()

        viewModel.useUserProfileDrawable(uid) {drawable ->
            toolbar.menu.findItem(R.id.action_profile)?.icon = drawable
            setSupportActionBar(toolbar)
            Log.i(TAG, "use drawable")
        }

        setSupportActionBar(toolbar)
    }

    private fun refreshDrinkData(){
        authState.performActionWithFreshTokens(authService) { accessToken, _, ex ->
            ex?.let{
                Log.e("$TAG refresh", it.error ?: "there was an error retrieving tokens")
                return@performActionWithFreshTokens
            }

            Log.i(TAG, accessToken ?: " ")
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
            accessToken?.let { viewModel.getUserInfo(it, authState.lastAuthorizationResponse!!.request.configuration.discoveryDoc!!.userinfoEndpoint!!) {uid ->
                prefs.edit().putString("uid", uid).apply()
            } }
        }
    }

    private fun getDrinkCredits(){
        authState.performActionWithFreshTokens(authService) { accessToken, _, ex ->
            ex?.let{
                Log.e("TAG drinkCredits", it.error ?: "there was an error getting drink credits")
                return@performActionWithFreshTokens
            }
            val uid = prefs.getString("uid", "")!!
            accessToken?.let { viewModel.getDrinkCredits(it, uid) {credits ->
                prefs.edit().putInt("credits", credits).apply()
                Log.i(TAG, credits.toString())
            } }
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

    private fun readAuthState(): AuthState {
        val stateJson = prefs.getString("stateJson", null)
        stateJson?.let {
            return AuthState.jsonDeserialize(stateJson)
        }
        return AuthState()
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
            return when (position) {
                0 -> "Little Drink"
                else -> "Big Drink"
            }
        }
    }
}
