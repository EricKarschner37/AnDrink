package rit.csh.andrink.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header.*
import kotlinx.coroutines.runBlocking
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
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this)
            .get(MainActivityViewModel::class.java)

        prefs = getSharedPreferences("auth", Context.MODE_PRIVATE)

        viewModel.setUid(prefs.getString("uid", "")!!)
        viewModel.setCredits(prefs.getInt("credits", 0))

        nav_view.setNavigationItemSelectedListener {

            when (it.itemId) {
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
        getUserInfo()

        drink_srl.isRefreshing = true
        refreshDrinkData()
        getDrinkCredits()

        setSupportActionBar(toolbar)

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
            val uid = prefs.getString("uid", "")!!
            accessToken?.let { viewModel.getDrinkCredits(it, uid) }
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

    private fun signOut() {
        val authPrefs = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val success = authPrefs.edit()
            .clear()
            .commit()

        if (success){
            val intent = Intent(this@MainActivity, SignInActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setProfileImage(){
        viewModel.useUserProfileDrawable(viewModel.uid.value!!) {drawable ->
            toolbar.menu.getItem(0).icon = drawable
            nav_profile_image.setImageDrawable(drawable.constantState?.newDrawable())
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

        viewModel.uid.observe(this, Observer { uid ->
            nav_username_view.text = "User: $uid"
            setProfileImage()
        })

        viewModel.credits.observe(this, Observer { credits ->
            nav_credits_view.text = "Credits: $credits"
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
            return when (position) {
                0 -> "Little Drink"
                else -> "Big Drink"
            }
        }
    }
}
