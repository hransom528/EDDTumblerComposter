package com.commanderhr1.eddtumblercomposter

//import android.Manifest
//import android.app.Activity

//import android.bluetooth.le.ScanCallback
//import android.os.Handler
//import android.os.Looper

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import java.util.*


class MainActivity : AppCompatActivity() {
    // WiFi
    private val wifi : WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    // Bluetooth
    private val bluetoothManager : BluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter : BluetoothAdapter? = bluetoothManager.adapter
    private val REQUEST_ENABLE_BT = 1
    // TODO: Generate UUID for Bluetooth service
    private val APP_UUID = UUID.randomUUID();

    // BLE
    /*protected val bleScanner = bluetoothAdapter.bluetoothLeScanner
    private var scanning = false
    private val SCAN_PERIOD: Long = 10000
    private var handler = Handler(Looper.getMainLooper())*/

    // Buttons
    private val btnWifiConnect: Button = findViewById(R.id.btnWifiConnect)
    private val swtWifiOn : SwitchCompat = findViewById(R.id.swtWifiOn)
    private val swtBTOn : SwitchCompat = findViewById(R.id.swtBTOn)

    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: Update Bluetooth code with server functionality
        if (bluetoothAdapter == null) {
            // Device doesn't support bluetooth
        }

        // Request Bluetooth permission if not enabled
        if (!bluetoothAdapter?.isEnabled!!) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        // Checks connect permission for Bluetooth
        checkBluetoothConnectPermission()

        // Checks app permissions for BLUETOOTH_CONNECT and notifies user if there is a permission mismatch
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "First enable Bluetooth Access in Settings!", Toast.LENGTH_LONG).show()
        }

        // Queries list of previously paired devices
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC Address
        }

        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

        // Enables Bluetooth discoverability
        val requestCode = 1;
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(
            BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        startActivityForResult(discoverableIntent, requestCode)

        //
        bluetoothAdapter.listenUsingRfcommWithServiceRecord("EDDTumblerComposterApp", APP_UUID)

        // TODO: Update preliminary WiFi code
        //val wifiSpecifierBuilder : WifiNetworkSpecifier.Builder = WifiNetworkSpecifier.Builder
        //wifi.isWifiEnabled = true
        val easyConnect = wifi.isEasyConnectSupported


        // btnConnect
        btnWifiConnect.setOnClickListener {

        }

        // swtBTOn
        swtBTOn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                bluetoothAdapter.enable()
            }
            else {
                bluetoothAdapter.disable()
            }
        }

        // swtBTOn
        swtWifiOn.setOnCheckedChangeListener { _, isChecked ->
            wifi.isWifiEnabled = isChecked
        }
    }

    // onDestroy
    override fun onDestroy() {
        super.onDestroy()

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }

    // Checks for BLUETOOTH_CONNECT permission
    private fun checkBluetoothConnectPermission() {
        // Checks app permissions for BLUETOOTH_CONNECT and notifies user if there is a permission mismatch
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "First enable Bluetooth Access in Settings!", Toast.LENGTH_LONG).show()
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action.toString()) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    checkBluetoothConnectPermission()
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                }
            }
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