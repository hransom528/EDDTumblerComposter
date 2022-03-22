package com.commanderhr1.eddtumblercomposter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.bluetooth.*
import android.content.Context
import android.net.wifi.*
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Switch


class MainActivity : AppCompatActivity() {
    // WiFi
    private val wifi : WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    // Bluetooth
    private val bluetoothManager : BluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter : BluetoothAdapter = bluetoothManager.adapter
    protected val bleScanner = bluetoothAdapter.bluetoothLeScanner
    private var scanning = false
    private val SCAN_PERIOD: Long = 10000
    private var handler = Handler(Looper.getMainLooper())

    // Buttons
    private val btnConnect: Button = findViewById(R.id.btnConnect)
    private val swtWifiOn : Switch = findViewById(R.id.swtWifiOn)

    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: Update preliminary Bluetooth code


        // TODO: Update preliminary WiFi code
        //val wifiSpecifier : WifiNetworkSpecifier =   WifiNetworkSpecifier.Builder
        //wifi.isWifiEnabled = true
        wifi.isEasyConnectSupported


        // btnConnect
        btnConnect.setOnClickListener {

        }
    }

    // Scans for BLE devices
    /*private fun scanBLEDevice() {
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bleScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bleScanner.startScan(leScanCallback)
        } else {
            scanning = false
            bleScanner.stopScan(leScanCallback)
        }
    }*/
}