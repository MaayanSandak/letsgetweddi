package com.example.letsgetweddi.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsgetweddi.adapters.SupplierAdapter
import com.example.letsgetweddi.databinding.FragmentDressesBinding
import com.example.letsgetweddi.model.Supplier
import com.google.firebase.database.*

class DressesFragment : Fragment() {

    private lateinit var binding: FragmentDressesBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: SupplierAdapter
    private val allSuppliers = mutableListOf<Supplier>()
    private val filteredSuppliers = mutableListOf<Supplier>()
    private val locationList = mutableListOf("All locations")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDressesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SupplierAdapter(filteredSuppliers)
        binding.recyclerDresses.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDresses.adapter = adapter

        database = FirebaseDatabase.getInstance().getReference("suppliers")

        database.orderByChild("category").equalTo("dresses")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    allSuppliers.clear()
                    locationList.clear()
                    locationList.add("All locations")

                    for (child in snapshot.children) {
                        val supplier = child.getValue(Supplier::class.java)
                        if (supplier != null) {
                            allSuppliers.add(supplier)
                            if (!supplier.location.isNullOrEmpty() && !locationList.contains(supplier.location!!)) {
                                locationList.add(supplier.location!!)
                            }
                        }
                    }

                    val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locationList)
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerLocationDresses.adapter = spinnerAdapter

                    filterSuppliers()
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        binding.searchViewDresses.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                filterSuppliers()
                return true
            }
        })

        binding.spinnerLocationDresses.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterSuppliers()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun filterSuppliers() {
        val query = binding.searchViewDresses.query.toString().lowercase()
        val selectedLocation = binding.spinnerLocationDresses.selectedItem.toString()

        filteredSuppliers.clear()
        filteredSuppliers.addAll(allSuppliers.filter { supplier ->
            val matchesQuery = supplier.name?.lowercase()?.contains(query) == true
            val matchesLocation = selectedLocation == "All locations" || supplier.location == selectedLocation
            matchesQuery && matchesLocation
        })

        adapter.notifyDataSetChanged()
    }
}
