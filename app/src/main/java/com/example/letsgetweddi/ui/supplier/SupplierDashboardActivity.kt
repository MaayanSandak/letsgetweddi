package com.example.letsgetweddi.ui.supplier

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.letsgetweddi.databinding.ActivitySupplierDashboardBinding
import com.example.letsgetweddi.ui.chat.ChatActivity
import com.example.letsgetweddi.utils.RoleManager
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class SupplierDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupplierDashboardBinding
    private var supplierId: String? = null
    private var imageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupplierDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        RoleManager.load(object : RoleManager.Callback {
            override fun onRoleLoaded(role: String, supplierId: String?) {
                if (role != "supplier" || supplierId.isNullOrEmpty()) {
                    Toast.makeText(this@SupplierDashboardActivity, "Supplier only", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }
                this@SupplierDashboardActivity.supplierId = supplierId
                loadSupplier()
                setupButtons()
            }
            override fun onNoUser() {
                finish()
            }
        })
    }

    private fun loadSupplier() {
        val id = supplierId ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Suppliers").child(id)
        ref.get().addOnSuccessListener { s ->
            val name = s.child("name").value?.toString() ?: ""
            val description = s.child("description").value?.toString() ?: ""
            val location = s.child("location").value?.toString() ?: ""
            val phone = s.child("phone").value?.toString() ?: ""
            imageUrl = s.child("imageUrl").value?.toString() ?: ""

            binding.editName.setText(name)
            binding.editDescription.setText(description)
            binding.editLocation.setText(location)
            binding.editPhone.setText(phone)
            if (imageUrl.isNotEmpty()) {
                Picasso.get().load(imageUrl).into(binding.imageSupplier)
            }
        }
    }

    private fun setupButtons() {
        binding.buttonSave.setOnClickListener {
            val id = supplierId ?: return@setOnClickListener
            val ref = FirebaseDatabase.getInstance().getReference("Suppliers").child(id)
            val map = mapOf(
                "name" to binding.editName.text.toString().trim(),
                "description" to binding.editDescription.text.toString().trim(),
                "location" to binding.editLocation.text.toString().trim(),
                "phone" to binding.editPhone.text.toString().trim()
            )
            ref.updateChildren(map).addOnSuccessListener {
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonManageAvailability.setOnClickListener {
            val id = supplierId ?: return@setOnClickListener
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("letsgetweddi://availability/$id"))
            startActivity(intent)
        }

        binding.buttonManageGallery.setOnClickListener {
            val id = supplierId ?: return@setOnClickListener
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("letsgetweddi://gallery/manage/$id"))
            startActivity(intent)
        }

        binding.buttonOpenChats.setOnClickListener {
            val id = supplierId ?: return@setOnClickListener
            val i = Intent(this, ChatActivity::class.java)
            i.putExtra("supplierId", id)
            startActivity(i)
        }
    }
}
