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
import d.stobie.currencyconverter.storage.DbHandler
import d.stobie.currencyconverter.utils.*
import org.json.JSONException
import java.lang.reflect.InvocationTargetException
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import java.util.*
import java.lang.ref.WeakReference
import java.util.concurrent.Semaphore


class MainActivity : AppCompatActivity() {

    var allCurrencies = ""
    val allCurrency = mutableMapOf<String?, String>()
    val everyCurrency = "Every Currency"
    private val url = "http://apilayer.net/api/list?access_key=438c79ea139b63e5a0175587b015bb2f"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val time = Date(System.currentTimeMillis())
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

            try {
                val intent = Intent(this, MyReceiver::class.java)
                intent.putExtra("URL", allCurrencies)
                intent.putExtra("CONVERT_FROM", "USD")

                val pendingIntent = PendingIntent.getBroadcast(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_CANCEL_CURRENT)

                val alarms = this.getSystemService(
                        Context.ALARM_SERVICE) as AlarmManager

                alarms.setRepeating(AlarmManager.RTC_WAKEUP,
                        time.getTime(),
                        AlarmManager.INTERVAL_HALF_HOUR,
                        pendingIntent)

            } catch (e: Exception) {
                e.printStackTrace()
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
        val url = API_CONVERSION_REQUEST + API_CURRENCIES + convertTo + API_SOURCE + convertFrom + API_FORMAT
        FetchCurrencies(this).execute(url)
    }


    private inner class FetchCurrencies internal constructor(context: MainActivity) : AsyncTask<String, Void, List<ConvertedCurrency>>() {

        private val activityReference: WeakReference<MainActivity> = WeakReference(context)

        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: String): List<ConvertedCurrency> {
            Log.d("Reponse", params[0])

            val data = mutableListOf<ConvertedCurrency>()
            val s = Semaphore(0)
            val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, params[0], null,
                    Response.Listener { response ->

                        try {
                            val quotes = response.getJSONObject("quotes")
                            val databaseHandler = DbHandler(applicationContext)
                            databaseHandler.reWriteDatabase()

                            //Iterate through the recieved JsonObject to get each key/value
                            val iter = quotes.keys()
                            while (iter.hasNext()) {
                                val key = iter.next()
                                try {

                                    val value = quotes.get(key)
                                    if (!key.isNullOrEmpty()) {
                                        val convertedCurrency = ConvertedCurrency(data.size, allCurrency.get(key.replace("USD", "")), value.toString())
                                        Log.d("Response", convertedCurrency.title + convertedCurrency.id)
                                        data.add(convertedCurrency)
                                        databaseHandler.addCurrency(convertedCurrency)
                                    }
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                            s.release()

                        } catch (e: InvocationTargetException) {
                            showToast("Invalid Input")
                            s.release()
                        }

                    },
                    Response.ErrorListener { error ->
                        error.printStackTrace()
                        showToast("Connection Error")
                        s.release()
                    }
            )
            MyApplication.getInstance(applicationContext).addToRequestQueue(jsonObjectRequest)

            s.acquire()
            Log.d("Response", data.size.toString())
            return data
        }

        override fun onPostExecute(result: List<ConvertedCurrency>) {
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return
            
            conversion_list.layoutManager = GridLayoutManager(applicationContext, 3)
            conversion_list.adapter = RecyclerAdapter(result)
            progressBar.visibility = View.INVISIBLE
            conversion_list.visibility = View.VISIBLE
        }
    }
}
