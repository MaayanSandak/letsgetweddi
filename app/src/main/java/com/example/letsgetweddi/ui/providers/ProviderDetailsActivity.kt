package com.example.letsgetweddi.ui.providers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsgetweddi.databinding.ActivityProviderDetailsBinding
import com.example.letsgetweddi.model.Review
import com.example.letsgetweddi.ui.chat.ChatActivity
import com.example.letsgetweddi.ui.gallery.GalleryFragment
import com.example.letsgetweddi.ui.supplier.SupplierDashboardActivity
import com.example.letsgetweddi.utils.RoleManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class ProviderDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProviderDetailsBinding

    private lateinit var reviewsRef: DatabaseReference
    private lateinit var reviewAdapter: com.example.letsgetweddi.adapters.ReviewAdapter
    private val reviewsList = mutableListOf<Review>()
    private val availabilityList = mutableListOf<String>()

    private var supplierId: String = ""
    private var supplierPhone: String = ""
    private var supplierName: String = ""
    private var supplierDescription: String = ""
    private var supplierLocation: String = ""
    private var supplierCategory: String = ""
    private var supplierImageUrl: String = ""

    private var isFavorite: Boolean = false
    private var currentRole: String = "client"
    private var currentUserSupplierId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supplierId = intent.getStringExtra("id") ?: intent.getStringExtra("supplierId") ?: return

        loadSupplierDetails()
        loadAvailability()
        setupButtons()
        loadReviews()
        preloadFavoriteState()
        preloadRoleAndOwnerUi()
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
                Picasso.get().load(supplierImageUrl).into(binding.imageProvider)
            } else {
                binding.imageProvider.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            val fragment = GalleryFragment.newInstance(supplierId)
            supportFragmentManager.beginTransaction()
                .replace(com.example.letsgetweddi.R.id.galleryContainer, fragment)
                .commit()
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
                val intent = Intent(Intent.ACTION_VIEW, "https://wa.me/$supplierPhone".toUri())
                startActivity(intent)
            } else {
                Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonFavorite.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser ?: return@setOnClickListener
            val userId = user.uid
            val favRef = FirebaseDatabase.getInstance().getReference("favorites").child(userId).child(supplierId)

            if (isFavorite) {
                favRef.removeValue().addOnSuccessListener {
                    isFavorite = false
                    updateFavoriteButton()
                    Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
                }
            } else {
                val supplierMap = mapOf(
                    "id" to supplierId,
                    "name" to supplierName,
                    "description" to supplierDescription,
                    "location" to supplierLocation,
                    "imageUrl" to supplierImageUrl,
                    "phone" to supplierPhone,
                    "category" to supplierCategory
                )
                favRef.setValue(supplierMap).addOnSuccessListener {
                    isFavorite = true
                    updateFavoriteButton()
                    Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonEditSupplier.setOnClickListener {
            startActivity(Intent(this, SupplierDashboardActivity::class.java))
        }
    }

    private fun preloadFavoriteState() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val userId = user.uid
        val favRef = FirebaseDatabase.getInstance().getReference("favorites").child(userId).child(supplierId)
        favRef.get().addOnSuccessListener { snapshot ->
            isFavorite = snapshot.exists()
            updateFavoriteButton()
        }
    }

    private fun updateFavoriteButton() {
        if (isFavorite) {
            binding.buttonFavorite.text = "Remove from favorites"
            binding.buttonFavorite.icon = androidx.core.content.ContextCompat.getDrawable(
                this, com.example.letsgetweddi.R.drawable.ic_favorite_24
            )
            binding.buttonFavorite.contentDescription = "Remove from favorites"
        } else {
            binding.buttonFavorite.text = "Add to favorites"
            binding.buttonFavorite.icon = androidx.core.content.ContextCompat.getDrawable(
                this, com.example.letsgetweddi.R.drawable.ic_favorite_border_24
            )
            binding.buttonFavorite.contentDescription = "Add to favorites"
        }
    }

    private fun preloadRoleAndOwnerUi() {
        RoleManager.load(object : RoleManager.Callback {
            override fun onRoleLoaded(role: String, supplierId: String?) {
                currentRole = role
                currentUserSupplierId = supplierId
                applyRoleUi()
            }
            override fun onNoUser() {
                currentRole = "client"
                currentUserSupplierId = null
                applyRoleUi()
            }
        })
    }

    private fun applyRoleUi() {
        val isOwner = currentRole == "supplier" && currentUserSupplierId == supplierId
        binding.buttonFavorite.isEnabled = currentRole == "client" && !isOwner
        binding.buttonSubmitReview.isEnabled = currentRole == "client" && !isOwner
        binding.buttonEditSupplier.visibility = if (isOwner) View.VISIBLE else View.GONE
    }

    private fun loadReviews() {
        reviewsRef = FirebaseDatabase.getInstance().getReference("reviews").child(supplierId)
        reviewAdapter = com.example.letsgetweddi.adapters.ReviewAdapter(reviewsList)
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
            val rating: Float = binding.ratingBarNewReview.rating
            val comment = binding.editReviewComment.text.toString().trim()
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null && rating > 0f) {
                val userId = user.uid
                val reviewId = reviewsRef.push().key ?: System.currentTimeMillis().toString()
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
