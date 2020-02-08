package rit.csh.drink.view


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_drink.*
import rit.csh.drink.R
import rit.csh.drink.model.Drink
import rit.csh.drink.model.MachineWithDrinks

class DrinkFragment(private val machineWithDrinks: MachineWithDrinks, private val onDrinkClicked: (Drink) -> Unit) : Fragment() {

    val pageTitle = machineWithDrinks.machine.displayName

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_drink, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        drink_rv.adapter = DrinkAdapter(requireContext(), onDrinkClicked)
        drink_rv.layoutManager = LinearLayoutManager(requireContext())

        setDrinks(machineWithDrinks.drinks)
    }

    private fun setDrinks(drinks: List<Drink>) {
        (drink_rv.adapter as DrinkAdapter).setDrinks(drinks)
    }

}
