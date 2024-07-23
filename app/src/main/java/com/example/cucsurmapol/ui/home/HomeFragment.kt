package com.example.cucsurmapol.ui.home

import android.os.Bundle
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
        val homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root



        val mapWebView: WebView = binding.mapWebView
        val webSettings: WebSettings = mapWebView.settings
        webSettings.javaScriptEnabled = true
        WebView.setWebContentsDebuggingEnabled(true)
        val navController = findNavController()
        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        mapWebView.addJavascriptInterface(WebAppInterface(requireContext(),navController,sharedViewModel), "Android")
        mapWebView.loadUrl("file:///android_asset/map.html")
        root.findViewById<Button>(R.id.refreshButton)
            .setOnClickListener{
                mapWebView.loadUrl("file:///android_asset/map.html")
            }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}