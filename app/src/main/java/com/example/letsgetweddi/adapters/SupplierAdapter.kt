package com.example.letsgetweddi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.letsgetweddi.R
import com.example.letsgetweddi.databinding.ItemSupplierBinding
import com.example.letsgetweddi.model.Supplier
import com.squareup.picasso.Picasso

class SupplierAdapter(private val suppliers: List<Supplier>) :
    RecyclerView.Adapter<SupplierAdapter.SupplierViewHolder>() {

    inner class SupplierViewHolder(private val binding: ItemSupplierBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(supplier: Supplier) {
            binding.textName.text = supplier.name
            binding.textDescription.text = supplier.description
            binding.textLocation.text = supplier.location
            Picasso.get().load(supplier.imageUrl).into(binding.imageSupplier)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplierViewHolder {
        val binding = ItemSupplierBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SupplierViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SupplierViewHolder, position: Int) {
        holder.bind(suppliers[position])
    }

    override fun getItemCount(): Int = suppliers.size
}
