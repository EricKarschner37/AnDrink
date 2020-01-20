package rit.csh.andrink.view


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_little_drink.*
import rit.csh.andrink.R
import rit.csh.andrink.model.Drink

class LittleDrinkFragment(private val onDrinkClicked: (Drink) -> Unit) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_little_drink, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        little_drink_rv.adapter = DrinkAdapter(requireContext(), onDrinkClicked)
        little_drink_rv.layoutManager = LinearLayoutManager(requireContext())
    }

    fun setDrinks(drinks: List<Drink>) {
        (little_drink_rv.adapter as DrinkAdapter).setDrinks(drinks)
    }
}
