package com.example.letsgetweddi.ui.providers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsgetweddi.adapters.ReviewAdapter
import com.example.letsgetweddi.databinding.ActivityProviderDetailsBinding
import com.example.letsgetweddi.model.Review
import com.example.letsgetweddi.ui.chat.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class ProviderDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProviderDetailsBinding
    private lateinit var reviewsRef: DatabaseReference
    private lateinit var reviewAdapter: ReviewAdapter
    private val reviewsList = mutableListOf<Review>()
    private val availabilityList = mutableListOf<String>()
    private var supplierId: String = ""
    private var supplierPhone: String = ""
    private var supplierName: String = ""
    private var supplierDescription: String = ""
    private var supplierLocation: String = ""
    private var supplierCategory: String = ""
    private var supplierImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supplierId = intent.getStringExtra("id") ?: return

        loadSupplierDetails()
        loadAvailability()
        setupButtons()
        loadReviews()
    }

    private fun loadSupplierDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Suppliers").child(supplierId)
        ref.get().addOnSuccessListener { snapshot ->
            supplierName = snapshot.child("name").value?.toString() ?: ""
            supplierDescription = snapshot.child("description").value?.toString() ?: ""
            supplierLocation = snapshot.child("location").value?.toString() ?: ""
            supplierCategory = snapshot.child("category").value?.toString() ?: ""
            supplierImageUrl = snapshot.child("imageUrl").value?.toString() ?: ""
            supplierPhone = snapshot.child("phone").value?.toString() ?: ""

            binding.textName.text = supplierName
            binding.textDescription.text = supplierDescription
            binding.textLocation.text = supplierLocation

            if (supplierImageUrl.isNotEmpty()) {
                Picasso.get().load(supplierImageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.imageProvider)
            } else {
                binding.imageProvider.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }

    private fun loadAvailability() {
        val ref = FirebaseDatabase.getInstance().getReference("SuppliersAvailability").child(supplierId)
        ref.get().addOnSuccessListener { snapshot ->
            availabilityList.clear()
            snapshot.children.forEach { dateSnap ->
                val date = dateSnap.key ?: ""
                availabilityList.add(date)
            }
            if (availabilityList.isEmpty()) {
                availabilityList.add("No available dates")
            }
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, availabilityList)
            binding.listAvailability.adapter = adapter
        }
    }

    private fun setupButtons() {
        binding.buttonChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("supplierId", supplierId)
            startActivity(intent)
        }

        binding.buttonContact.setOnClickListener {
            if (supplierPhone.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$supplierPhone"))
                startActivity(intent)
            } else {
                Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonFavorite.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val userId = user.uid
                val supplierMap = mapOf(
                    "id" to supplierId,
                    "name" to supplierName,
                    "description" to supplierDescription,
                    "location" to supplierLocation,
                    "imageUrl" to supplierImageUrl,
                    "phone" to supplierPhone,
                    "category" to supplierCategory
                )
                FirebaseDatabase.getInstance().getReference("favorites")
                    .child(userId)
                    .child(supplierId)
                    .setValue(supplierMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun loadReviews() {
        reviewsRef = FirebaseDatabase.getInstance().getReference("reviews").child(supplierId)
        reviewAdapter = ReviewAdapter(reviewsList)
        binding.recyclerReviews.layoutManager = LinearLayoutManager(this)
        binding.recyclerReviews.adapter = reviewAdapter

        reviewsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reviewsList.clear()
                for (child in snapshot.children) {
                    val review = child.getValue(Review::class.java)
                    if (review != null) reviewsList.add(review)
                }
                reviewAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        binding.buttonSubmitReview.setOnClickListener {
            val rating = binding.ratingBarNewReview.rating
            val comment = binding.editReviewComment.text.toString().trim()
            val user = FirebaseAuth.getInstance().currentUser ?: return@setOnClickListener
            val userId = user.uid

            if (rating > 0 && comment.isNotEmpty()) {
                val reviewId = reviewsRef.push().key ?: return@setOnClickListener
                val newReview = Review(
                    userId = userId,
                    name = user.displayName ?: "Anonymous",
                    rating = rating,
                    comment = comment,
                    timestamp = System.currentTimeMillis()
                )
                reviewsRef.child(reviewId).setValue(newReview)
                binding.editReviewComment.setText("")
                binding.ratingBarNewReview.rating = 0f
            }
        }
    }
}
