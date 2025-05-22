package com.example.clouddart

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class SearchFlightsActivity : AppCompatActivity() {
    private lateinit var departureDate: TextView
    private lateinit var returnDate: TextView
    private lateinit var guestsCount: TextView
    private lateinit var searchButton: Button
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_flights)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        departureDate = findViewById<TextView>(R.id.departureDateText)
        returnDate = findViewById<TextView>(R.id.returnDateText)
        guestsCount = findViewById<TextView>(R.id.guestsText)
        searchButton = findViewById(R.id.btnSearchFlights)

        // Set initial dates
        departureDate.text = dateFormatter.format(calendar.time)
    }

    private fun setupClickListeners() {
        // Close button
        findViewById<ImageButton>(R.id.closeButton).setOnClickListener {
            finish()
        }

        // Date pickers
        departureDate.setOnClickListener {
            showDatePicker(true)
        }

        returnDate.setOnClickListener {
            showDatePicker(false)
        }

        // Search button
        searchButton.setOnClickListener {
            performSearch()
        }
    }

    private fun showDatePicker(isDeparture: Boolean) {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                val formattedDate = dateFormatter.format(calendar.time)
                if (isDeparture) {
                    departureDate.text = formattedDate
                } else {
                    returnDate.text = formattedDate
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun performSearch() {
        // Implement search logic here
        // You can gather all the input data and process it

        // For now, just finish the activity
        finish()
    }
}