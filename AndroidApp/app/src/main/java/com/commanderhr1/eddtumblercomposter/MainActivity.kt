package com.commanderhr1.eddtumblercomposter

//import android.Manifest
//import android.app.Activity

//import android.bluetooth.le.ScanCallback
//import android.os.Handler
//import android.os.Looper
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import com.google.android.material.switchmaterial.SwitchMaterial


class MainActivity : AppCompatActivity() {
    // WiFi
    private val wifi : WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    // Bluetooth
    private val bluetoothManager : BluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter : BluetoothAdapter? = bluetoothManager.adapter
    private val REQUEST_ENABLE_BT = 1

    // BLE
    /*protected val bleScanner = bluetoothAdapter.bluetoothLeScanner
    private var scanning = false
    private val SCAN_PERIOD: Long = 10000
    private var handler = Handler(Looper.getMainLooper())*/

    // Buttons
    private val btnConnect: Button = findViewById(R.id.btnConnect)
    // TODO: Change swtWifiOn to SwitchCompat
    private val swtWifiOn : Switch = findViewById(R.id.swtWifiOn)

    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: Update Bluetooth code with scanning functionality
        if (bluetoothAdapter == null) {
            // Device doesn't support bluetooth
        }

        // Request Bluetooth permission if not enabled
        if (!bluetoothAdapter?.isEnabled!!) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        // Checks app permissions for BLUETOOTH_CONNECT and notifies user if there is a permission mismatch
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "First enable Bluetooth Access in Settings!", Toast.LENGTH_LONG).show()
        }

        // Queries list of previously paired devices
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC Address
        }


        // TODO: Update preliminary WiFi code
        //val wifiSpecifier : WifiNetworkSpecifier =   WifiNetworkSpecifier.Builder
        //wifi.isWifiEnabled = true
        val easyConnect = wifi.isEasyConnectSupported


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