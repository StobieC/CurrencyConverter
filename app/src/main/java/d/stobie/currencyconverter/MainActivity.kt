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
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.GridLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import d.stobie.currencyconverter.utils.*


class MainActivity : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager
    var allCurrencies = ""
    private val url = "http://apilayer.net/api/list?access_key=438c79ea139b63e5a0175587b015bb2f"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        conversion_list.layoutManager = GridLayoutManager(this, 3)
        fetchCurrencyList()

        convert_amount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                // TODO Auto-generated method stub
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // TODO Auto-generated method stub
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                if (convert_to_et.text.toString() == "ALL") {
                    convertCurrencies(convert_from_et.text.toString(), allCurrencies, convert_amount.text.toString())
                } else if  (convert_from_et.text.isNotEmpty() && convert_to_et.text.isNotEmpty()) {
                    convertCurrencies(convert_from_et.text.toString(), convert_to_et.text.toString(), convert_amount.text.toString())
                }
            }
        })

        convert_btn.setOnClickListener {

            convertCurrencies("USD", convert_to_et.text.toString(), convert_amount.text.toString())
        }

    }

    private fun fetchCurrencyList() {
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->

                    val currencies = response.getJSONObject("currencies").names()
                    val currencyTitles = response.getJSONObject("currencies")
                    val responseArray = mutableListOf<String>()

                    Log.d("Response", response.getJSONObject("currencies").toString())
                    Log.d("Response", response.getJSONObject("currencies").names().length().toString())

                    responseArray.add("ALL")

                    for (i in 0 until currencies.length() ) {
                        responseArray.add(currencies.get(i).toString())
                        Log.d("Response", currencies.get(i).toString())
                        if (i>0)
                            allCurrencies = allCurrencies.plus(",").plus(currencies.get(i).toString())
                        else
                            allCurrencies = currencies.get(i).toString()
                    }

                    Log.d("Response", allCurrencies)

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

    private fun convertCurrencies(convertFrom: String, convertTo: String, convertAmount: String) {
        //API_CONVERSION_REQUEST + API_CURRENCIES + convertTo + API_SOURCE + convertFrom + API_FORMAT
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, API_CONVERSION_REQUEST + API_CURRENCIES + convertTo + API_SOURCE + convertFrom + API_FORMAT, null,
                Response.Listener { response ->
                    Log.d("Response", response.toString())
                },
                Response.ErrorListener { error ->
                    error.printStackTrace()
                    showToast("Connection Error")
                }
        )
        MyApplication.getInstance(this).addToRequestQueue(jsonObjectRequest)
        Log.d("Response",jsonObjectRequest.toString())
    }

}
