package rit.csh.andrink.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel

class LaunchActivityViewModel(application: Application): AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun userIsAuthorized(): Boolean{
        return prefs.contains("stateJson")
    }
}