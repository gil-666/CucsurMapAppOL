package com.example.cucsurmapol

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.cucsurmapol.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding


    @SuppressLint("SetJavaScriptEnabled")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkLocationPermission()
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home -> navView.menu.findItem(R.id.navigation_home).isChecked = true
                R.id.navigation_dashboard -> navView.menu.findItem(R.id.navigation_dashboard).isChecked = true
                // Add more cases for other destinations if needed
            }
        }



        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }
                R.id.navigation_dashboard -> {
                    navController.navigate(R.id.navigation_dashboard)
                    true
                }
                R.id.navigation_notifications -> {
                    navController.navigate(R.id.navigation_notifications)
                    true
                }
                else -> false
            }
        }


    }
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_CODE)
        } else {
            startLocationUpdates()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                startLocationUpdates()
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_home)
            } else {
                // Check if the user has checked "Don't ask again"
                val shouldShowRequestPermissionRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )

                if (!shouldShowRequestPermissionRationale) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Permiso de ubicación")
                    builder.setMessage("El permiso de ubicación es necesario para mostrar tu ubicación en el mapa. Por favor, habilítalo desde los ajustes de la aplicación para usar todas las funciones.")

                    builder.setPositiveButton("Ir a ajustes") { dialog, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                        dialog.dismiss()
                    }

                    builder.setNegativeButton("Negar") { dialog, _ ->
                        dialog.dismiss()
                    }

                    val dialog = builder.create()
                    dialog.show()
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Permiso de ubicación")
                    builder.setMessage("El permiso de ubicación es necesario para mostrar tu ubicación en el mapa. Algunas funciones no estarán disponibles si lo niegas.")

                    builder.setPositiveButton("Dar permiso") { dialog, _ ->
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            REQUEST_PERMISSION_CODE
                        )
                        dialog.dismiss()
                    }

                    builder.setNegativeButton("Negar") { dialog, _ ->
                        dialog.dismiss()
                    }

                    val dialog = builder.create()
                    dialog.show()
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }


    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        if (!navController.popBackStack()) {
            super.onBackPressed()
        }
    }

    companion object {
        const val REQUEST_PERMISSION_CODE = 100
         lateinit var fusedLocationClient: FusedLocationProviderClient
         lateinit var locationRequest: LocationRequest
         lateinit var locationCallback: LocationCallback
        fun updateOpenLayersLocation(latitude: Double, longitude: Double, WebView:WebView) {
            val json = JSONObject().apply {
                put("latitude", latitude)
                put("longitude", longitude)
            }

            // method to js
            WebView.evaluateJavascript("updateLocation($json)", null)

        }
    }
}