package com.example.finaldemo.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

object SimHelper {

    @SuppressLint("MissingPermission")
    fun getCarrierNames(context: Context): List<String> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return emptyList()
        }

        val carrierNames = mutableSetOf<String>()
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                val activeSubscriptions = subscriptionManager?.activeSubscriptionInfoList ?: emptyList()
                for (subInfo in activeSubscriptions) {
                    subInfo.carrierName?.toString()?.let { carrierNames.add(it) }
                }
            } else {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                telephonyManager?.networkOperatorName?.let { carrierNames.add(it) }
            }
        } catch (e: Exception) {
            // Can throw SecurityException on some devices, even with permission.
            e.printStackTrace()
        }
        
        return carrierNames.toList()
    }
}
