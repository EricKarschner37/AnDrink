package rit.csh.drink.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import rit.csh.drink.R

data class MachineWithDrinks(
    @Embedded val machine: Machine,
    @Relation(
        parentColumn = "name",
        entityColumn = "machine"
    )
    val drinks: List<Drink>
)

@Entity
data class Machine(
    @PrimaryKey val name: String,
    val displayName: String,
    val isOnline: Boolean
) {
    fun getDrawableForStatus() =
        if (isOnline){
            R.drawable.indicator_online
        } else {
            R.drawable.indicator_offline
        }
}