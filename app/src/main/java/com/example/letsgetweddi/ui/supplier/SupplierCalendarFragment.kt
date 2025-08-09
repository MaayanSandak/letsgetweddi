package com.example.letsgetweddi.ui.supplier

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsgetweddi.databinding.FragmentSupplierCalendarBinding
import com.google.firebase.database.*

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SupplierCalendarFragment : Fragment() {

    private var _binding: FragmentSupplierCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: DatabaseReference
    private lateinit var adapter: SupplierDatesAdapter
    private val dates = ArrayList<String>()
    private var supplierId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supplierId = arguments?.getString(ARG_SUPPLIER_ID).orEmpty()
        db = FirebaseDatabase.getInstance().getReference("SuppliersAvailability").child(supplierId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSupplierCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SupplierDatesAdapter(dates, object : SupplierDatesAdapter.Listener {
            override fun onRemove(dateIso: String) {
                db.child(dateIso).removeValue()
            }
        })
        binding.recyclerDates.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDates.adapter = adapter

        binding.calendarView.setOnDateChangeListener { _: CalendarView, year: Int, month: Int, dayOfMonth: Int ->
            val iso = toIso(year, month, dayOfMonth)
            toggleDate(iso)
        }

        observeDates()
    }

    private fun observeDates() {
        binding.progressBar.visibility = View.VISIBLE
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dates.clear()
                for (c in snapshot.children) {
                    val key = c.key ?: continue
                    val isAvailable = c.getValue(Boolean::class.java) ?: false
                    if (isAvailable) dates.add(key)
                }
                dates.sort()
                adapter.notifyDataSetChanged()
                binding.textEmpty.visibility = if (dates.isEmpty()) View.VISIBLE else View.GONE
                binding.progressBar.visibility = View.GONE
            }
            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    private fun toggleDate(iso: String) {
        db.child(iso).get().addOnSuccessListener { snap ->
            val exists = snap.exists() && (snap.getValue(Boolean::class.java) == true)
            if (exists) {
                db.child(iso).removeValue()
            } else {
                db.child(iso).setValue(true)
            }
        }
    }

    private fun toIso(year: Int, monthZeroBased: Int, day: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthZeroBased)
        cal.set(Calendar.DAY_OF_MONTH, day)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(cal.time)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_SUPPLIER_ID = "supplierId"
        fun newInstance(supplierId: String): SupplierCalendarFragment {
            val f = SupplierCalendarFragment()
            f.arguments = Bundle().apply { putString(ARG_SUPPLIER_ID, supplierId) }
            return f
        }
    }
}
