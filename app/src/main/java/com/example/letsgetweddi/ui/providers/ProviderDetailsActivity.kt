package com.example.letsgetweddi.ui.providers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val supplierId = intent.getStringExtra("id") ?: return
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
                val supplierMap = mapOf(
                    "id" to supplierId,
                    "name" to name,
                    "description" to description,
                    "location" to location,
                    "imageUrl" to imageUrl,
                    "phone" to phone,
                    "category" to intent.getStringExtra("category")
                )
                FirebaseDatabase.getInstance().getReference("favorites")
                    .child(userId)
                    .child(supplierId)
                    .setValue(supplierMap)
            }
        }


        val user = FirebaseAuth.getInstance().currentUser ?: return
        val userId = user.uid

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
