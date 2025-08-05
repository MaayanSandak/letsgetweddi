package com.example.letsgetweddi.ui.supplier

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.letsgetweddi.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*

class EditSupplierFragment : Fragment() {

    private lateinit var imageSupplier: ImageView
    private lateinit var buttonSelectImage: Button
    private lateinit var editName: EditText
    private lateinit var editDescription: EditText
    private lateinit var editLocation: EditText
    private lateinit var editCategory: EditText
    private lateinit var editPhone: EditText
    private lateinit var buttonSave: Button
    private var imageUri: Uri? = null
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_supplier, container, false)

        imageSupplier = view.findViewById(R.id.imageSupplier)
        buttonSelectImage = view.findViewById(R.id.buttonSelectImage)
        editName = view.findViewById(R.id.editName)
        editDescription = view.findViewById(R.id.editDescription)
        editLocation = view.findViewById(R.id.editLocation)
        editCategory = view.findViewById(R.id.editCategory)
        editPhone = view.findViewById(R.id.editPhone)
        buttonSave = view.findViewById(R.id.buttonSaveSupplier)

        loadSupplierData()

        buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        buttonSave.setOnClickListener {
            saveSupplierData()
        }

        return view
    }

    private fun loadSupplierData() {
        val ref = FirebaseDatabase.getInstance().getReference("Suppliers").child(uid)
        ref.get().addOnSuccessListener { snapshot ->
            editName.setText(snapshot.child("name").value?.toString() ?: "")
            editDescription.setText(snapshot.child("description").value?.toString() ?: "")
            editLocation.setText(snapshot.child("location").value?.toString() ?: "")
            editCategory.setText(snapshot.child("category").value?.toString() ?: "")
            editPhone.setText(snapshot.child("phone").value?.toString() ?: "")

            val imageUrl = snapshot.child("imageUrl").value?.toString() ?: ""
            if (imageUrl.isNotEmpty()) {
                Picasso.get().load(imageUrl).into(imageSupplier)
            }
        }
    }

    private fun saveSupplierData() {
        val name = editName.text.toString().trim()
        val description = editDescription.text.toString().trim()
        val location = editLocation.text.toString().trim()
        val category = editCategory.text.toString().trim()
        val phone = editPhone.text.toString().trim()

        if (name.isEmpty() || description.isEmpty() || location.isEmpty() || category.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            val storageRef = FirebaseStorage.getInstance()
                .getReference("supplier_images/$uid/${UUID.randomUUID()}")
            storageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveToDatabase(name, description, location, category, phone, uri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveToDatabase(name, description, location, category, phone, null)
        }
    }

    private fun saveToDatabase(
        name: String,
        description: String,
        location: String,
        category: String,
        phone: String,
        imageUrl: String?
    ) {
        val ref = FirebaseDatabase.getInstance().getReference("Suppliers").child(uid)
        val supplierData = mutableMapOf(
            "name" to name,
            "description" to description,
            "location" to location,
            "category" to category,
            "phone" to phone
        )
        if (imageUrl != null) {
            supplierData["imageUrl"] = imageUrl
        }

        ref.setValue(supplierData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Supplier details saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save details", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data?.data != null) {
            imageUri = data.data
            imageSupplier.setImageURI(imageUri)
        }
    }
}
