package rit.csh.andrink.model

import androidx.lifecycle.LiveData
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Machine(
    val drinks: LiveData<List<Drink>>,
    @PrimaryKey val name: String,
    val displayName: String,
    val status: Status
)

enum class Status{
    ONLINE,
    OFFLINE,
    UNKNOWN
}