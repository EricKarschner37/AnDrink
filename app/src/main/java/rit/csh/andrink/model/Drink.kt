package rit.csh.andrink.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import kotlinx.android.parcel.Parcelize
import org.json.JSONArray
import org.json.JSONObject

@Entity
@Parcelize
data class Drink(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name="label") val name: String,
    @ColumnInfo(name="cost") val cost: Int,
    @ColumnInfo(name="slot") val slot: Int,
    @ColumnInfo(name="isStocked") val isActive: Boolean,
    @ColumnInfo(name="machine") val machine: String
): Parcelable{

    companion object {

        fun parseJsonToDrinks(slots: JSONArray, machineName: String): List<Drink> {
            val drinks = mutableListOf<Drink>()
            for (j in 0 until slots.length()){
                val slot = slots.getJSONObject(j)
                val item = slot.getJSONObject("item")
                val drink = Drink(
                    0,
                    item.getString("name"),
                    item.getInt("price"),
                    slot.getInt("number"),
                    !slot.getBoolean("empty") && slot.getBoolean("active"),
                    machineName
                )
                drinks.add(drink)
            }

            return drinks
        }
    }
}