package com.example.cucsurmapol.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cucsurmapol.DatabaseHelper
import com.example.cucsurmapol.R
import com.example.cucsurmapol.SharedViewModel
import com.example.cucsurmapol.WebAppInterface
import com.example.cucsurmapol.databinding.FragmentHomeBinding


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

        val navController = findNavController()
        val webAppint = WebAppInterface(requireContext(), navController, sharedViewModel);
        mapWebView.addJavascriptInterface(webAppint, "Android")
        mapWebView.loadUrl("file:///android_asset/map.html")
//        sharedViewModel.dbLoaded.postValue(true)

        root.findViewById<Button>(R.id.refreshButton).setOnClickListener {
            sharedViewModel.dbLoaded.postValue(false)
            Handler(Looper.getMainLooper()).post {
                webAppint.dbHelper.loadDatabase(requireContext())
                mapWebView.loadUrl("file:///android_asset/map.html")
            }
        }



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}