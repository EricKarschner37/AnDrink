package rit.csh.andrink.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import org.json.JSONObject

@Entity(tableName = "drink_table")
data class Drink(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name="label") val name: String,
    @ColumnInfo(name="cost") val cost: Int,
    @ColumnInfo(name="machine") val machine: String,
    @ColumnInfo(name="slot") val slot: Int,
    @ColumnInfo(name="machineId") val machineId: Int,
    @ColumnInfo(name="isStocked") val isActive: Boolean
)

fun parseJsonToDrinks(json: JSONObject): List<Drink> {
    val drinks = mutableListOf<Drink>()
    val machines = json.getJSONArray("machines")
    for (i in 0 until machines.length()) {
        val machine = machines.getJSONObject(i)
        val slots = machine.getJSONArray("slots")
        for (j in 0 until slots.length()){
            val slot = slots.getJSONObject(j)
            val item = slot.getJSONObject("item")
            val drink = Drink(
                0,
                item.getString("name"),
                item.getInt("price"),
                machine.getString("name"),
                slot.getInt("number"),
                machine.getInt("id"),
                !slot.getBoolean("empty") && slot.getBoolean("active")
            )
            drinks.add(drink)
        }
    }

    return drinks
}