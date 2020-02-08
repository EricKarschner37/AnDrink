package rit.csh.drink.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val uid: String,
    var credits: Int,
    var isCurrent: Boolean
)