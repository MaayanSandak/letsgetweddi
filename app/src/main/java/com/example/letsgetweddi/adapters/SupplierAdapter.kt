package com.example.letsgetweddi.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.letsgetweddi.databinding.ItemSupplierBinding
import com.example.letsgetweddi.model.Supplier
import com.example.letsgetweddi.ui.providers.ProviderDetailsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class SupplierAdapter(
    private val suppliers: MutableList<Supplier>,
    private val isFavorites: Boolean = false
) : RecyclerView.Adapter<SupplierAdapter.SupplierViewHolder>() {

    inner class SupplierViewHolder(private val binding: ItemSupplierBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(supplier: Supplier) {
            binding.textName.text = supplier.name
            binding.textDescription.text = supplier.description
            binding.textLocation.text = supplier.location
            Picasso.get().load(supplier.imageUrl).into(binding.imageSupplier)

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, ProviderDetailsActivity::class.java)
                intent.putExtra("id", supplier.id)
                intent.putExtra("name", supplier.name)
                intent.putExtra("description", supplier.description)
                intent.putExtra("location", supplier.location)
                intent.putExtra("imageUrl", supplier.imageUrl)
                intent.putExtra("phone", supplier.phone)
                intent.putExtra("category", supplier.category)
                context.startActivity(intent)
            }

            if (isFavorites) {
                binding.buttonRemove.visibility = View.VISIBLE
                binding.buttonRemove.setOnClickListener {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                    val supplierId = supplier.id ?: return@setOnClickListener

                    FirebaseDatabase.getInstance().getReference("favorites")
                        .child(userId)
                        .child(supplierId)
                        .removeValue()

                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        suppliers.removeAt(position)
                        notifyItemRemoved(position)
                    }
                }
            } else {
                binding.buttonRemove.visibility = View.GONE
            }
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
