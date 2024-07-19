package com.example.cucsurmapol.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cucsurmapol.SharedViewModel
import com.example.cucsurmapol.WebAppInterface
import com.example.cucsurmapol.databinding.FragmentDashboardBinding


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
                ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val mapWebView: WebView = binding.WebView
        val webSettings: WebSettings = mapWebView.settings
        webSettings.javaScriptEnabled = true
        WebView.setWebContentsDebuggingEnabled(true)
        val navController = findNavController()
        mapWebView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return false
            }
        })
        val info = arguments?.getString("info")
        var infourl = "file:///android_asset/info.html"

        if (!info.isNullOrBlank()){
            infourl+"?info=$info"
        }
        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        mapWebView.addJavascriptInterface(WebAppInterface(requireContext(),navController,sharedViewModel), "Android")
        mapWebView.loadUrl(infourl)
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}