package com.pndnwngi.billumaba.util

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

fun isGooglePlayServicesAvailable(context: Context): Boolean {
    val availability = GoogleApiAvailability.getInstance()
    return availability.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
}
