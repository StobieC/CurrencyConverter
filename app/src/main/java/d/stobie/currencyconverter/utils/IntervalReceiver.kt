package d.stobie.currencyconverter.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import d.stobie.currencyconverter.model.ConvertedCurrency
import d.stobie.currencyconverter.storage.DbHandler
import d.stobie.currencyconverter.volleytools.MyApplication
import org.json.JSONException
import java.lang.Error

class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val urls = intent.getStringExtra("URL")
        val convertFrom = intent.getStringExtra("CONVERT_FROM")

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, API_CONVERSION_REQUEST + API_CURRENCIES + urls + API_SOURCE + convertFrom + API_FORMAT, null,
                Response.Listener { response ->

                    try {
                        val quotes = response.getJSONObject("quotes")
                        val databaseHandler = DbHandler(context)

                        val data = mutableListOf<ConvertedCurrency>()
                        //Iterate through the recieved JsonObject to get each key/value
                        val iter = quotes.keys()
                        while (iter.hasNext()) {
                            val key = iter.next()
                            try {

                                val value = quotes.get(key)
                                if (!key.isNullOrEmpty()) {

                                    val convertedCurrency = ConvertedCurrency(data.size, key, value.toString())
                                    data.add(convertedCurrency)
                                    databaseHandler.reWriteDatabase()
                                    databaseHandler.addCurrency(convertedCurrency)
                                }

                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }

                    } catch (e: Error) {
                       e.printStackTrace()
                    }

                },
                Response.ErrorListener { error ->
                    error.printStackTrace()
                }
        )
        MyApplication.getInstance(context).addToRequestQueue(jsonObjectRequest)
        Log.d("Response", jsonObjectRequest.toString())
    }
}