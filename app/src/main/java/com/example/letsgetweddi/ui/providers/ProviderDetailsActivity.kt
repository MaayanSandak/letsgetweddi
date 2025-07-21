package com.example.letsgetweddi.ui.providers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.letsgetweddi.databinding.ActivityProviderDetailsBinding
import com.example.letsgetweddi.ui.chat.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class ProviderDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProviderDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("name") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val location = intent.getStringExtra("location") ?: ""
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val phone = intent.getStringExtra("phone") ?: ""

        binding.textName.text = name
        binding.textDescription.text = description
        binding.textLocation.text = location

        Picasso.get().load(imageUrl).into(binding.imageProvider)

        binding.buttonChat.setOnClickListener {
            val supplierId = intent.getStringExtra("id") ?: return@setOnClickListener
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("supplierId", supplierId)
            startActivity(intent)
        }

        binding.buttonContact.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phone"))
            startActivity(intent)
        }

        binding.buttonFavorite.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val userId = user.uid
                val supplierId = intent.getStringExtra("id") ?: return@setOnClickListener

                val supplierMap = mapOf(
                    "id" to supplierId,
                    "name" to intent.getStringExtra("name"),
                    "description" to intent.getStringExtra("description"),
                    "location" to intent.getStringExtra("location"),
                    "imageUrl" to intent.getStringExtra("imageUrl"),
                    "phone" to intent.getStringExtra("phone"),
                    "category" to intent.getStringExtra("category")
                )

                FirebaseDatabase.getInstance().getReference("favorites")
                    .child(userId)
                    .child(supplierId)
                    .setValue(supplierMap)
            }
        }

    }
}
