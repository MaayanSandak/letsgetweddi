package com.example.letsgetweddi.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.letsgetweddi.R
import com.example.letsgetweddi.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private var userType: String = "Client"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        drawerLayout = binding.drawerLayout
        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_suppliers,
                R.id.nav_tips_and_checklist,
                R.id.nav_profile
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        FirebaseDatabase.getInstance().getReference("Users")
            .child(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                userType = snapshot.child("userType").value.toString()
                if (userType == "Supplier") {
                    navView.menu.clear()
                    navView.inflateMenu(R.menu.supplier_main_drawer)
                } else {
                    navView.menu.clear()
                    navView.inflateMenu(R.menu.client_main_drawer)
                }
                setupNavMenu()
            }
    }

    private fun setupNavMenu() {
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> navController.navigate(R.id.nav_home)
                R.id.nav_suppliers -> navController.navigate(R.id.nav_suppliers)
                R.id.nav_calendar -> if (userType == "Supplier") {
                    navController.navigate(R.id.nav_supplier_calendar)
                }
                R.id.nav_edit_supplier -> if (userType == "Supplier") {
                    navController.navigate(R.id.nav_edit_supplier)
                }
                R.id.nav_all_suppliers -> navController.navigate(R.id.nav_all_suppliers)
                R.id.nav_tips_and_checklist -> navController.navigate(R.id.nav_tips_and_checklist)
                R.id.nav_favorites -> navController.navigate(R.id.nav_favorites)
                R.id.nav_profile -> navController.navigate(R.id.nav_profile)
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
