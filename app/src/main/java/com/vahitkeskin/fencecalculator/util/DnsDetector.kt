package com.vahitkeskin.fencecalculator.util

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object DnsDetector {
    /**
     * Checks if Private DNS is enabled on the device.
     * This checks for the "private_dns_mode" setting which can be "off", "opportunistic", or "hostname".
     * Ad-blocking DNS services usually set this to "hostname".
     */
    fun isPrivateDnsEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val resolver = context.contentResolver
            val mode = Settings.Global.getString(resolver, "private_dns_mode")
            // "off" means disabled.
            // "opportunistic" means Automatic (might use encrypted DNS if available)
            // "hostname" means Private DNS provider hostname is set.
            // We consider it "open" if it's not "off" or null.
            mode != null && mode != "off"
        } else {
            false
        }
    }

    /**
     * More advanced check to see if we are currently using a private DNS provider name.
     */
    fun getPrivateDnsServerName(context: Context): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork
            val lp = cm.getLinkProperties(network)
            return lp?.privateDnsServerName
        }
        return null
    }

    /**
     * Attempts to open the Private DNS settings directly, with fallbacks.
     */
    fun openDnsSettings(context: Context) {
        // Try more specific Private DNS settings first (Standard for Android 9+)
        val privateDnsIntent = Intent("android.settings.PRIVATE_DNS_SETTINGS").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        // Try "More Connection Settings" which the user specifically asked for
        val wirelessIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            // First attempt: More connection settings (as requested)
            context.startActivity(wirelessIntent)
        } catch (e: Exception) {
            try {
                // Fallback 1: Private DNS Direct
                context.startActivity(privateDnsIntent)
            } catch (e2: Exception) {
                // Fallback 2: General Settings
                try {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                } catch (e3: Exception) {
                    // Final fallback ignored
                }
            }
        }
    }
}
