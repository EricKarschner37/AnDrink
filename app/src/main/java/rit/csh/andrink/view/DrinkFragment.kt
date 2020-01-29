package rit.csh.andrink.view


import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_drink.*
import rit.csh.andrink.R
import rit.csh.andrink.model.Drink
import rit.csh.andrink.model.Machine
import rit.csh.andrink.model.Status

class DrinkFragment(private val machine: Machine, private val onDrinkClicked: (Drink) -> Unit) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_drink, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        machine.drinks.observe(viewLifecycleOwner, Observer { drinks ->
            setDrinks(drinks)
        })

        machine.status.observe(viewLifecycleOwner, Observer { status ->
            when(status!!){
                Status.UNKNOWN -> {
                    status_view.text = "Unknown"
                    status_view.background = resources.getColor(R.color.statusUnknown).toDrawable()
                }
                Status.ONLINE -> {
                    status_view.text = "Online"
                    status_view.background = resources.getColor(R.color.statusOnline).toDrawable()
                }
                Status.OFFLINE -> {
                    status_view.text = "Offline"
                    status_view.background = resources.getColor(R.color.statusOffline).toDrawable()
                }
            }
        })

        drink_rv.adapter = DrinkAdapter(requireContext(), onDrinkClicked)
        drink_rv.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setDrinks(drinks: List<Drink>) {
        (drink_rv.adapter as DrinkAdapter).setDrinks(drinks)
    }

}
