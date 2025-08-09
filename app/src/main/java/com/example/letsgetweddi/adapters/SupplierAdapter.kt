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
            val context = binding.root.context

            binding.textName.text = supplier.name ?: ""
            binding.textDescription.text = supplier.description ?: ""
            binding.textLocation.text = supplier.location ?: ""

            val imageUrl = supplier.imageUrl
            if (!imageUrl.isNullOrEmpty()) {
                Picasso.get().load(imageUrl).into(binding.imageSupplier)
            } else {
                binding.imageSupplier.setImageDrawable(null)
            }

            binding.root.setOnClickListener {
                val intent = Intent(context, ProviderDetailsActivity::class.java).apply {
                    putExtra("id", supplier.id)
                    putExtra("name", supplier.name)
                    putExtra("description", supplier.description)
                    putExtra("location", supplier.location)
                    putExtra("imageUrl", supplier.imageUrl)
                    putExtra("phone", supplier.phone)
                    putExtra("category", supplier.category)
                }
                context.startActivity(intent)
            }

            binding.buttonRemove.visibility = if (isFavorites) View.VISIBLE else View.GONE
            binding.buttonRemove.setOnClickListener {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                val supplierId = supplier.id ?: return@setOnClickListener

                FirebaseDatabase.getInstance().getReference("favorites")
                    .child(uid)
                    .child(supplierId)
                    .removeValue()
                    .addOnSuccessListener {
                        val index = bindingAdapterPosition
                        if (index != RecyclerView.NO_POSITION) {
                            suppliers.removeAt(index)
                            notifyItemRemoved(index)
                        }
                    }
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
