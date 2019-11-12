package d.stobie.currencyconverter.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import d.stobie.currencyconverter.model.ConvertedCurrency

class RecyclerAdapter(private val currencyList: List<ConvertedCurrency>) : RecyclerView.Adapter<CurrencyViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CurrencyViewHolder {
        val inflater = LayoutInflater.from(p0.context)
        return CurrencyViewHolder(inflater, p0)
    }

    override fun getItemCount(): Int {
        return currencyList.size
    }

    override fun onBindViewHolder(p0: CurrencyViewHolder, p1: Int) {
        val convertedCurrency: ConvertedCurrency = currencyList[p1]
        p0.bind(convertedCurrency)
    }
}