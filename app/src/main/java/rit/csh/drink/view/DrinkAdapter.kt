package rit.csh.drink.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.drink_item.view.*
import rit.csh.drink.R
import rit.csh.drink.model.drink.Drink

class DrinkAdapter internal constructor(private val context: Context, private val onDrinkClicked: (Drink) -> Unit) : RecyclerView.Adapter<DrinkAdapter.ViewHolder>() {

    private val inflater = LayoutInflater.from(context)
    private var drinks = emptyList<Drink>()

    override fun getItemCount(): Int = drinks.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = inflater.inflate(R.layout.drink_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drink = drinks[position]
        holder.nameView.text = drink.name
        holder.costView.text = "${drink.cost} credits"
        if (drink.isActive){
            holder.dropButton.setOnClickListener {
                onDrinkClicked.invoke(drink)
            }
        } else {
            Log.i("DrinkAdapter", drink.toString())
            holder.nameView.alpha = 0.25F
            holder.costView.alpha = 0.25F
            holder.dropButton.alpha = 0.25F
            holder.dropButton.setOnClickListener {  }
        }
    }

    internal fun setDrinks(drinks: List<Drink>) {
        this.drinks = drinks
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val nameView = itemView.drink_name_tv
        val costView = itemView.drink_cost_tv
        val dropButton = itemView.drink_drop_btn
    }
}