package d.stobie.currencyconverter.viewHolder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import d.stobie.currencyconverter.R
import d.stobie.currencyconverter.model.ConvertedCurrency

class CurrencyViewHolder (inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.conversion_list_item, parent, false)) {

    private var titleTxt: TextView? = null
    private var conversionTxt: TextView? = null

    init {
        titleTxt = itemView.findViewById(R.id.conversion_title_txt)
        conversionTxt = itemView.findViewById(R.id.conversion_amount_txt)
    }

    fun bind(currency: ConvertedCurrency) {
        titleTxt?.text = currency.title
        conversionTxt?.text = currency.amount
    }

}