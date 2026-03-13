package com.vahitkeskin.fencecalculator.ui.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.LinkProperties
import com.vahitkeskin.fencecalculator.util.DnsDetector

fun CalculatorViewModel.checkPrivateDnsExt() {
    isPrivateDnsEnabled = DnsDetector.isPrivateDnsEnabled(context)
}

fun CalculatorViewModel.startDnsMonitoringExt() {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            checkPrivateDnsExt()
        }
        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            checkPrivateDnsExt()
        }
        override fun onLost(network: Network) {
            checkPrivateDnsExt()
        }
    })
}
