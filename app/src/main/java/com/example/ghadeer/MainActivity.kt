package com.example.ghadeer

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.TextureView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var currentBalanceTV: TextView
    lateinit var rcV: RecyclerView
    lateinit var depositETV: EditText
    lateinit var withdrawTV: EditText
    lateinit var depositBN: Button
    lateinit var withdrawBN: Button
    lateinit var historyList: ArrayList<String>
    lateinit var sharedPreferences: SharedPreferences

    var atm = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentBalanceTV = findViewById(R.id.tvBalance)
        rcV = findViewById(R.id.rvHistory)
        depositETV = findViewById(R.id.etDeposit)
        withdrawTV = findViewById(R.id.etWithdraw)
        depositBN = findViewById(R.id.btnDeposit)
        withdrawBN = findViewById(R.id.btnWithdraw)
        historyList = arrayListOf()

        updatRV()
        lodData()

        depositBN.setOnClickListener {
            deposit()
        }

        withdrawBN.setOnClickListener {
            withdraw()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putFloat("currentBalance", getBalance())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setBalanceView(savedInstanceState.getFloat("currentBalance", 0f))
    }

    private fun deposit() {
        if (depositETV.text.isEmpty()) {
            Snackbar.make(clMain, "Please enter a number", Snackbar.LENGTH_LONG).show()
            return
        }
        val depositAmount = depositETV.text.toString().toFloat()
        historyList.add("Deposit: $depositAmount")
        depositETV.setText("")

        val currentBalance = getBalance() + depositAmount
        setBalanceView(currentBalance)
        with(sharedPreferences.edit()) {
            putFloat("balance", currentBalance)
            apply()
        }
        rcvAdapter()
    }

    private fun getBalance(): Float {
        return currentBalanceTV.text.toString().removePrefix("Current Balance: ").toFloat()
    }

    private fun setBalanceView(balance: Float) {
        currentBalanceTV.text = "Current Balance: $balance"
        when {
            balance == 0f -> currentBalanceTV.setTextColor(Color.WHITE)
            balance > 0f -> currentBalanceTV.setTextColor(Color.BLACK)
            balance < 0f -> currentBalanceTV.setTextColor(Color.RED)
        }
    }

    private fun withdraw() {
        if (withdrawTV.text.isEmpty()) {
            Snackbar.make(clMain, "Please enter a number", Snackbar.LENGTH_LONG).show()
            return
        }

        if (getBalance() < 0) {
            Snackbar.make(clMain, "Insufficient funds", Snackbar.LENGTH_LONG).show()
            return
        }

        val withdrawAmount = withdrawTV.text.toString().toFloat()
        var currentBalance = getBalance() - withdrawAmount
        historyList.add("Withdrawal: $withdrawAmount")
        withdrawTV.setText("")

        if (currentBalance < 0) {
            historyList.add("Negative Balance Fee: 20")
            currentBalance -= 20
        }

        setBalanceView(currentBalance)
        with(sharedPreferences.edit()) {
            putFloat("balance", currentBalance)
            apply()
        }
        rcvAdapter()
    }

    //Recycler view...
    private fun updatRV() {
        rcV.adapter = RecyclerViewAdapter(historyList)
        rcV.layoutManager = LinearLayoutManager(this)
    }

    private fun rcvAdapter() {
        rcV.adapter!!.notifyDataSetChanged()
        rcV.scrollToPosition(historyList.size - 1)
    }

    //FOR sharedPreferences...
    private fun lodData() {
        sharedPreferences =
            this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        atm = sharedPreferences.getFloat("balance", 0f)
    }


    //Menu linking
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item, menu)
        return true
    }

    // Ledger (historyList) clearing
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clear -> {
                historyList.clear()
                rcV.adapter!!.notifyDataSetChanged()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }



}