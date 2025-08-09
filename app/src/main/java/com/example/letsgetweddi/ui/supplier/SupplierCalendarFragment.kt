package com.example.letsgetweddi.ui.supplier

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsgetweddi.databinding.FragmentSupplierCalendarBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class SupplierCalendarFragment : Fragment(), SupplierDatesAdapter.Listener {

    private var _binding: FragmentSupplierCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var supplierId: String
    private val dates = mutableListOf<String>()
    private lateinit var adapter: SupplierDatesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supplierId = requireArguments().getString(ARG_SUPPLIER_ID)
            ?: throw IllegalStateException("supplierId is required")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSupplierCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SupplierDatesAdapter(dates, this)
        binding.recyclerDates.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDates.adapter = adapter

        binding.progressBar.visibility = View.GONE
        binding.textEmpty.visibility = View.GONE

        loadDates()

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val iso = toIso(year, month, dayOfMonth)
            addDate(iso)
        }

        binding.btnClearAll.setOnClickListener {
            clearAllDates()
        }
    }

    private fun dbRef() =
        FirebaseDatabase.getInstance()
            .getReference("SuppliersAvailability")
            .child(supplierId)

    private fun loadDates() {
        binding.progressBar.visibility = View.VISIBLE
        dbRef().addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dates.clear()
                for (child in snapshot.children) {
                    child.key?.let { dates.add(it) }
                }
                dates.sort()
                adapter.notifyDataSetChanged()
                binding.textEmpty.visibility = if (dates.isEmpty()) View.VISIBLE else View.GONE
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addDate(iso: String) {
        binding.progressBar.visibility = View.VISIBLE
        dbRef().child(iso).setValue(true).addOnCompleteListener { task ->
            binding.progressBar.visibility = View.GONE
            if (task.isSuccessful) {
                if (!dates.contains(iso)) {
                    val insertAt = (dates + iso).sorted()
                    dates.clear()
                    dates.addAll(insertAt)
                    adapter.notifyDataSetChanged()
                    binding.textEmpty.visibility = if (dates.isEmpty()) View.VISIBLE else View.GONE
                }
                Toast.makeText(requireContext(), "Date added: $iso", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Add failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRemove(dateIso: String) {
        binding.progressBar.visibility = View.VISIBLE
        dbRef().child(dateIso).removeValue().addOnCompleteListener { task ->
            binding.progressBar.visibility = View.GONE
            if (task.isSuccessful) {
                val idx = dates.indexOf(dateIso)
                if (idx >= 0) {
                    dates.removeAt(idx)
                    adapter.notifyItemRemoved(idx)
                }
                binding.textEmpty.visibility = if (dates.isEmpty()) View.VISIBLE else View.GONE
                Toast.makeText(requireContext(), "Date removed: $dateIso", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Remove failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearAllDates() {
        if (dates.isEmpty()) return
        binding.progressBar.visibility = View.VISIBLE
        dbRef().removeValue().addOnCompleteListener { task ->
            binding.progressBar.visibility = View.GONE
            if (task.isSuccessful) {
                dates.clear()
                adapter.notifyDataSetChanged()
                binding.textEmpty.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "All dates cleared", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Clear failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toIso(year: Int, monthZeroBased: Int, day: Int): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)
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
