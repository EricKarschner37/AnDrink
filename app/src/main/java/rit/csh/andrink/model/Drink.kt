package rit.csh.andrink.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drink_table")
data class Drink(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name="label") val name: String,
    @ColumnInfo(name="cost") val cost: Int,
    @ColumnInfo(name="machine") val machine: String
)