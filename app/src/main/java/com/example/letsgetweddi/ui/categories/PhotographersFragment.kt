package com.example.letsgetweddi.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsgetweddi.adapters.SupplierAdapter
import com.example.letsgetweddi.databinding.FragmentPhotographersBinding
import com.example.letsgetweddi.model.Supplier
import com.google.firebase.database.*

class PhotographersFragment : Fragment() {

    private lateinit var binding: FragmentPhotographersBinding
    private lateinit var database: DatabaseReference
    private val suppliers = mutableListOf<Supplier>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPhotographersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerPhotographers.layoutManager = LinearLayoutManager(requireContext())
        val adapter = SupplierAdapter(suppliers)
        binding.recyclerPhotographers.adapter = adapter

        database = FirebaseDatabase.getInstance().getReference("suppliers")
        database.orderByChild("category").equalTo("photographers")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    suppliers.clear()
                    for (child in snapshot.children) {
                        val supplier = child.getValue(Supplier::class.java)
                        if (supplier != null) suppliers.add(supplier)
                    }
                    adapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
