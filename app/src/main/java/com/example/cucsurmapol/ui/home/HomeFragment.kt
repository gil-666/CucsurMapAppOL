package com.example.cucsurmapol.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cucsurmapol.DatabaseHelper
import com.example.cucsurmapol.MainActivity
import com.example.cucsurmapol.MainActivity.Companion.fusedLocationClient
import com.example.cucsurmapol.MainActivity.Companion.locationCallback
import com.example.cucsurmapol.MainActivity.Companion.locationRequest
import com.example.cucsurmapol.MainActivity.Companion.updateOpenLayersLocation
import com.example.cucsurmapol.R
import com.example.cucsurmapol.SharedViewModel
import com.example.cucsurmapol.WebAppInterface
import com.example.cucsurmapol.databinding.FragmentHomeBinding
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        (activity as AppCompatActivity).supportActionBar?.hide()

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val root: View = binding.root

        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        val mapWebView: WebView = binding.mapWebView
        val webSettings: WebSettings = mapWebView.settings
        webSettings.javaScriptEnabled = true
        WebView.setWebContentsDebuggingEnabled(true)
        webSettings.domStorageEnabled = true
        mapWebView.settings.setGeolocationEnabled(true)
        mapWebView.settings.setMediaPlaybackRequiresUserGesture(false)
        mapWebView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
                callback?.invoke(origin, true, false)
            }
        }
        val navController = findNavController()
        val webAppint = WebAppInterface(requireContext(), navController, sharedViewModel,parentFragmentManager);
        mapWebView.addJavascriptInterface(webAppint, "App")
        mapWebView.loadUrl("file:///android_asset/map.html")
//        sharedViewModel.dbLoaded.postValue(true)

        root.findViewById<Button>(R.id.refreshButton).setOnClickListener {
            sharedViewModel.dbLoaded.postValue(false)
            Handler(Looper.getMainLooper()).post {
                webAppint.dbHelper.loadDatabase(requireContext())
                mapWebView.loadUrl("file:///android_asset/map.html")
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMaxUpdates(1) // Number of location updates
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                location?.let {
                        updateOpenLayersLocation(it.latitude, it.longitude,mapWebView)
                }
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}