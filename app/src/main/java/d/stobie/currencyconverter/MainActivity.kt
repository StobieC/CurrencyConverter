package d.stobie.currencyconverter

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import d.stobie.currencyconverter.volleytools.MyApplication
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v7.widget.GridLayoutManager
import android.view.View
import d.stobie.currencyconverter.adapter.RecyclerAdapter
import d.stobie.currencyconverter.model.ConvertedCurrency
import d.stobie.currencyconverter.utils.*
import org.json.JSONException
import java.lang.reflect.InvocationTargetException

class MainActivity : AppCompatActivity() {

    var allCurrencies = ""
    val allCurrency = mutableMapOf<String?, String>()
    val everyCurrency = "Every Currency"
    private val url = "http://apilayer.net/api/list?access_key=438c79ea139b63e5a0175587b015bb2f"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        conversion_list.layoutManager = GridLayoutManager(this, 3)
        fetchCurrencyList()

        convert_btn.setOnClickListener {

            if (convert_to_et.text.toString().isNullOrEmpty()) {
                convert_to_et.setText(everyCurrency)
            }
            //Check that entered currency is legitimate
            if (convert_to_et.text.toString() == everyCurrency || allCurrency.get(convert_to_et.text.toString()) != null) {
                if (convert_to_et.text.toString() == everyCurrency) {
                    convertCurrencies("USD", allCurrencies, convert_amount.text.toString())
                } else if (convert_from_et.text.isNotEmpty() && convert_to_et.text.isNotEmpty()) {
                    convertCurrencies("USD", convert_to_et.text.toString().replace(" ", ""), convert_amount.text.toString())
                }
            }
        }

    }

    /**
     * Aquires list of available currencies to convert to
     */
    private fun fetchCurrencyList() {
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->

                    val currencies = response.getJSONObject("currencies").names()
                    val currency = response.getJSONObject("currencies")
                    val responseArray = mutableListOf<String>()

                    Log.d("Response", response.getJSONObject("currencies").toString())
                    Log.d("Response", response.getJSONObject("currencies").names().length().toString())

                    responseArray.add("Every Currency")

                    //TODO move into iterator
                    for (i in 0 until currencies.length()) {
                        responseArray.add(currencies.get(i).toString())
                        if (i > 0)
                            allCurrencies = allCurrencies.plus(",").plus(currencies.get(i).toString())
                        else
                            allCurrencies = currencies.get(i).toString()
                    }

                    val iter = currency.keys()
                    while (iter.hasNext()) {
                        val key = iter.next()
                        try {
                            val value = currency.get(key).toString()
                            allCurrency.put(key, value)
                            responseArray.add(key)

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }

                    val adapter = ArrayAdapter<String>(this,
                            android.R.layout.simple_dropdown_item_1line, responseArray)

                    convert_from_et.setAdapter(adapter)
                    convert_to_et.setAdapter(adapter)
                },
                Response.ErrorListener { error ->
                    error.printStackTrace()
                    showToast("Connection Error")
                }
        )
        // Access the RequestQueue through your singleton class.
        MyApplication.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    /**
     * convert the selected currency to the selected currencies
     * ***Free API does not support convert amount*** defaults to 1****
     */
    private fun convertCurrencies(convertFrom: String, convertTo: String, convertAmount: String) {
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, API_CONVERSION_REQUEST + API_CURRENCIES + convertTo + API_SOURCE + convertFrom + API_FORMAT, null,
                Response.Listener { response ->

                    try {
                        val quotes = response.getJSONObject("quotes")

                        val data = mutableListOf<ConvertedCurrency>()
                        //Iterate through the recieved JsonObject to get each key/value
                        val iter = quotes.keys()
                        while (iter.hasNext()) {
                            val key = iter.next()
                            try {

                                val value = quotes.get(key)
                                if (!key.isNullOrEmpty()) {
                                    val convertedCurrency = ConvertedCurrency(allCurrency.get(key.replace("USD", "")), value.toString())
                                    data.add(convertedCurrency)
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                        conversion_list.layoutManager = GridLayoutManager(this, 3)
                        conversion_list.adapter = RecyclerAdapter(data)
                        conversion_list.visibility = View.VISIBLE
                    } catch (e: InvocationTargetException) {
                        showToast("Invalid Input")
                    }

                },
                Response.ErrorListener { error ->
                    error.printStackTrace()
                    showToast("Connection Error")
                }
        )
        MyApplication.getInstance(this).addToRequestQueue(jsonObjectRequest)
        Log.d("Response", jsonObjectRequest.toString())
    }

}
