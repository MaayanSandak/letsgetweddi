package com.example.letsgetweddi.ui.supplier

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.letsgetweddi.databinding.ItemAvailableDateBinding

class SupplierDatesAdapter(
    private val data: MutableList<String>,
    private val listener: Listener
) : RecyclerView.Adapter<SupplierDatesAdapter.VH>() {

    interface Listener {
        fun onRemove(dateIso: String)
    }

    inner class VH(val b: ItemAvailableDateBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAvailableDateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val date = data[position]
        holder.b.textDate.text = date
        holder.b.btnRemove.setOnClickListener { listener.onRemove(date) }
    }

    override fun getItemCount(): Int = data.size
}
