package com.example.cucsurmapol.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val mapWebView: WebView = binding.mapWebView
        val webSettings: WebSettings = mapWebView.settings
        webSettings.javaScriptEnabled = true
        WebView.setWebContentsDebuggingEnabled(true)
        mapWebView.addJavascriptInterface(WebAppInterface(requireContext()), "Android")
        mapWebView.loadUrl("file:///android_asset/map.html")


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}