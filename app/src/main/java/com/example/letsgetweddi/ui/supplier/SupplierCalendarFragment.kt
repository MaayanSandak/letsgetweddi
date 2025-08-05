package com.example.letsgetweddi.ui.supplier

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.letsgetweddi.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class SupplierCalendarFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var saveButton: Button
    private lateinit var datesListView: ListView
    private val selectedDates = mutableListOf<String>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_supplier_calendar, container, false)

        calendarView = view.findViewById(R.id.calendarView)
        saveButton = view.findViewById(R.id.buttonSaveDate)
        datesListView = view.findViewById(R.id.listViewDates)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            if (!selectedDates.contains(date)) {
                selectedDates.add(date)
                updateList()
            }
        }

        saveButton.setOnClickListener {
            saveDatesToFirebase()
        }

        loadDatesFromFirebase()

        return view
    }

    private fun updateList() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, selectedDates)
        datesListView.adapter = adapter
    }

    private fun saveDatesToFirebase() {
        val ref = FirebaseDatabase.getInstance().getReference("SuppliersAvailability").child(uid)
        val datesMap = selectedDates.associateWith { true }
        ref.setValue(datesMap)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Availability saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save availability", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadDatesFromFirebase() {
        val ref = FirebaseDatabase.getInstance().getReference("SuppliersAvailability").child(uid)
        ref.get().addOnSuccessListener { snapshot ->
            selectedDates.clear()
            snapshot.children.forEach { dateSnap ->
                selectedDates.add(dateSnap.key ?: "")
            }
            updateList()
        }
    }
}
