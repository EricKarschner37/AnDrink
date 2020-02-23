package rit.csh.drink.model.drink

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import rit.csh.drink.R
import java.util.*

data class MachineWithDrinks(
    @Embedded val machine: Machine,
    @Relation(
        parentColumn = "name",
        entityColumn = "machine",
        entity = Drink::class
    )
    private val drinkStock: List<Drink>
){
    val drinks: List<Drink>
    get() = drinkStock.sorted()
}

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