package com.example.letsgetweddi.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsgetweddi.adapters.SupplierAdapter
import com.example.letsgetweddi.databinding.FragmentFavoritesBinding
import com.example.letsgetweddi.model.Supplier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavoritesFragment : Fragment() {

    private lateinit var binding: FragmentFavoritesBinding
    private lateinit var database: DatabaseReference
    private val favorites = mutableListOf<Supplier>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerFavorites.layoutManager = LinearLayoutManager(requireContext())
        val adapter = SupplierAdapter(favorites, isFavorites = true)
        binding.recyclerFavorites.adapter = adapter

        val user = FirebaseAuth.getInstance().currentUser ?: return
        val userId = user.uid

        database = FirebaseDatabase.getInstance().getReference("favorites").child(userId)
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favorites.clear()
                for (child in snapshot.children) {
                    val supplier = child.getValue(Supplier::class.java)
                    if (supplier != null) favorites.add(supplier)
                }
                adapter.notifyDataSetChanged()

                if (favorites.isEmpty()) {
                    binding.textEmpty.visibility = View.VISIBLE
                } else {
                    binding.textEmpty.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
