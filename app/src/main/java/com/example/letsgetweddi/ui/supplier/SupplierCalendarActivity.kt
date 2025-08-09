package com.example.letsgetweddi.ui.supplier

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.letsgetweddi.databinding.ActivitySupplierCalendarBinding
import com.example.letsgetweddi.utils.RoleManager

class SupplierCalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupplierCalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupplierCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val explicitId = intent.getStringExtra("supplierId")
        val deepLinkId = extractSupplierIdFromDeepLink(intent?.data)
        if (explicitId != null || deepLinkId != null) {
            openFragment(explicitId ?: deepLinkId!!)
        } else {
            RoleManager.load(object : RoleManager.Callback {
                override fun onRoleLoaded(role: String, supplierId: String?) {
                    if (role == "supplier" && !supplierId.isNullOrEmpty()) {
                        openFragment(supplierId)
                    } else {
                        finish()
                    }
                }
                override fun onNoUser() {
                    finish()
                }
            })
        }
    }

    private fun openFragment(supplierId: String) {
        val f = SupplierCalendarFragment.newInstance(supplierId)
        supportFragmentManager.beginTransaction()
            .replace(com.example.letsgetweddi.R.id.calendarContainer, f)
            .commit()
    }

    private fun extractSupplierIdFromDeepLink(data: Uri?): String? {
        if (data == null) return null
        val path = data.path ?: return null
        val parts = path.trim('/').split('/')
        return parts.lastOrNull()
    }
}
