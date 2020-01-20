package rit.csh.andrink.view


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_big_drink.*
import kotlinx.android.synthetic.main.fragment_little_drink.*
import rit.csh.andrink.R
import rit.csh.andrink.model.Drink

class BigDrinkFragment(private val onDrinkClicked: (Drink) -> Unit) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_big_drink, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        big_drink_rv.adapter = DrinkAdapter(requireContext(), onDrinkClicked)
        big_drink_rv.layoutManager = LinearLayoutManager(requireContext())
    }

    fun setDrinks(drinks: List<Drink>) {
        (big_drink_rv.adapter as DrinkAdapter).setDrinks(drinks)
    }

}
