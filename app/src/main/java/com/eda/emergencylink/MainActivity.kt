package com.eda.emergencylink

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Night = Color(0xFF0B1220)
private val Panel = Color(0xFF172235)
private val Accent = Color(0xFFFF4D5A)
private val Mint = Color(0xFF53D6B0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = Night, surface = Panel)) { EmergencyHome() } }
    }
}

@Composable
private fun EmergencyHome() {
    val context = LocalContext.current
    var locationText by remember { mutableStateOf("Location not checked yet") }
    var networkText by remember { mutableStateOf("Checking network…") }
    var wifiText by remember { mutableStateOf("Tap Scan Wi-Fi to inspect visible networks") }
    var sosText by remember { mutableStateOf("SOS message is ready; sending is not confirmed until a recipient/app confirms it.") }
    var scanCount by remember { mutableIntStateOf(0) }
    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.any { it }) locationText = getLocationText(context) else locationText = "Location permission was not granted"
    }
    val wifiPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        scanCount++
        wifiText = scanWifi(context)
    }

    LaunchedEffect(Unit) { networkText = getNetworkText(context) }

    Surface(modifier = Modifier.fillMaxSize(), color = Night) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = RoundedCornerShape(14.dp), color = Accent, modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Sos, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp)) }
                }
                Column {
                    Text("EDA EMERGENCY LINK", fontSize = 21.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Emergency communication helper · 0.3", fontSize = 12.sp, color = Mint)
                }
            }
            Surface(color = Color(0xFF321C2A), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("SOS / I NEED HELP", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Prepare your emergency message and share it through any available app or service.", color = Color(0xFFFFDDE0))
                    Button(onClick = {
                        val message = "SOS — I NEED HELP. My location: $locationText. Time: " +
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()) +
                            ". Sent from EDA Emergency Link."
                        sosText = "Share menu opened. Delivery is not yet confirmed."
                        val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, message) }
                        context.startActivity(Intent.createChooser(intent, "Share emergency SOS"))
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Accent)) {
                        Icon(Icons.Default.Sos, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("SHARE EMERGENCY SOS", fontWeight = FontWeight.Bold)
                    }
                    Text(sosText, fontSize = 12.sp, color = Color(0xFFFFDDE0))
                }
            }
            InfoCard("NETWORK STATUS", Icons.Default.NetworkCheck, networkText) {
                OutlinedButton(onClick = { networkText = getNetworkText(context) }, modifier = Modifier.fillMaxWidth()) { Text("Refresh connection status") }
            }
            InfoCard("GPS / LAST KNOWN LOCATION", Icons.Default.LocationOn, locationText) {
                Button(onClick = {
                    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (fine || coarse) locationText = getLocationText(context)
                    else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }, modifier = Modifier.fillMaxWidth()) { Text("Get available location") }
            }
            InfoCard("VISIBLE WI-FI NETWORKS", Icons.Default.Wifi, wifiText) {
                Button(onClick = {
                    val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
                    if (android.os.Build.VERSION.SDK_INT >= 33) permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
                    wifiPermission.launch(permissions.toTypedArray())
                }, modifier = Modifier.fillMaxWidth()) { Text("Scan Wi-Fi") }
                OutlinedButton(onClick = {
                    context.startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
                }, modifier = Modifier.fillMaxWidth()) { Text("Open Android Wi-Fi settings") }
            }
            Surface(color = Panel, shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("IMPORTANT LIMITATIONS", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("This prototype cannot create satellite service, bypass Wi-Fi passwords, or guarantee Internet access. Satellite messaging and phone-to-phone mesh are not implemented. Always use official emergency services when available.", color = Color(0xFFB9C6D8), fontSize = 13.sp)
                }
            }
            Text("Offline-first prototype · Always verify message delivery", color = Color(0xFF7F90A8), fontSize = 11.sp)
        }
    }
}

@Composable
private fun InfoCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, body: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(color = Panel, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = Mint)
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Text(body, color = Color(0xFFB9C6D8), fontSize = 13.sp)
            content()
        }
    }
}

private fun getNetworkText(context: Context): String {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return "No active network detected. This does not test satellite availability."
    val caps = cm.getNetworkCapabilities(network) ?: return "Network details unavailable."
    val kinds = mutableListOf<String>()
    if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) kinds.add("Wi-Fi")
    if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) kinds.add("Cellular")
    if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) kinds.add("Ethernet")
    if (caps.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) kinds.add("Bluetooth")
    val internet = if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) "Internet validated" else "Internet not verified"
    return (if (kinds.isEmpty()) "Active network detected" else kinds.joinToString()) + " · " + internet
}

@Suppress("MissingPermission")
private fun getLocationText(context: Context): String {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = manager.getProviders(true)
    var best: Location? = null
    for (provider in providers) {
        val loc = try { manager.getLastKnownLocation(provider) } catch (_: SecurityException) { null }
        if (loc != null && (best == null || loc.time > best.time)) best = loc
    }
    return best?.let {
        "Lat: %.6f, Lon: %.6f · accuracy about %.0f m".format(Locale.US, it.latitude, it.longitude, it.accuracy)
    } ?: "No last-known location available. Try outdoors with location enabled."
}

@Suppress("MissingPermission")
private fun scanWifi(context: Context): String {
    return try {
        val manager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!manager.isWifiEnabled) return "Wi-Fi is turned off. Turn it on in Android settings, then scan again."
        manager.startScan()
        val results = manager.scanResults
        if (results.isEmpty()) "No scan results yet. Android may restrict scans; wait a moment and try again."
        else results.sortedByDescending { it.level }.take(12).joinToString("\n") {
            val security = if (it.capabilities.contains("WEP") || it.capabilities.contains("PSK") || it.capabilities.contains("EAP") || it.capabilities.contains("SAE")) "Secured" else "May be open"
            "• ${it.SSID.ifBlank { "(hidden network)" }} · $security · ${it.level} dBm"
        }
    } catch (e: Exception) { "Wi-Fi scan unavailable: ${e.javaClass.simpleName}" }
}
