package rit.csh.andrink.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Entity
import androidx.room.PrimaryKey
import rit.csh.andrink.R

@Entity
data class Machine(
    val drinks: LiveData<List<Drink>>,
    @PrimaryKey val name: String,
    val displayName: String,
    var status: Status
){
    fun setStatus(isOnline: Boolean){
        status = if (isOnline){
            Status.ONLINE
        } else {
            Status.OFFLINE
        }
    }
}

enum class Status{
    ONLINE {
        override val indicator: Int = R.drawable.indicator_online
    },
    OFFLINE {
        override val indicator: Int = R.drawable.indicator_offline
    },
    UNKNOWN {
        override val indicator: Int = R.drawable.indicator_unknown
    };

    abstract val indicator: Int
}