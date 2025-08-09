package com.example.letsgetweddi.ui.supplier

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.letsgetweddi.R
import com.example.letsgetweddi.databinding.ActivitySupplierCalendarBinding
import com.example.letsgetweddi.utils.RoleManager

class SupplierCalendarActivity : AppCompatActivity(), RoleManager.Callback {

    private lateinit var binding: ActivitySupplierCalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupplierCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val supplierIdFromIntent = intent.getStringExtra("supplierId")
        if (!supplierIdFromIntent.isNullOrBlank()) {
            openFragment(supplierIdFromIntent)
            return
        }

        val supplierIdFromDeepLink = extractSupplierIdFromDeepLink(intent?.data)
        if (!supplierIdFromDeepLink.isNullOrBlank()) {
            openFragment(supplierIdFromDeepLink)
            return
        }

        RoleManager.load(this)
    }

    override fun onRoleLoaded(role: String, supplierId: String?) {
        if (role == "supplier" && !supplierId.isNullOrBlank()) {
            openFragment(supplierId)
        } else {
            finish()
        }
    }

    override fun onNoUser() {
        finish()
    }

    private fun openFragment(supplierId: String) {
        val f = SupplierCalendarFragment.newInstance(supplierId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.calendarContainer, f)
            .commit()
    }

    private fun extractSupplierIdFromDeepLink(data: Uri?): String? {
        if (data == null) return null
        val path = data.path ?: return null
        val parts = path.trim('/').split('/')
        return parts.lastOrNull()
    }
}
