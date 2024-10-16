package com.abovepersonal.aiportfolio

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class Utils {
    companion object {
        fun isNetworkConnected(context: Context): Boolean{
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

            if (connectivityManager != null) {
                val network = connectivityManager.activeNetwork
                if (network != null) {
                    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                    if (networkCapabilities != null) {
                        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    }
                }
            }
            return false
        }

        fun message(
            title: String,
            description: String,
            buttonText: String,
            onButtonClick: () -> Unit = {}
        ){
            messageDescription.value = description
            messageTitle.value = title
            messageButtonText.value = buttonText
            messageOnClick.value = onButtonClick

            isMessageVisible.value = true
        }
    }
}