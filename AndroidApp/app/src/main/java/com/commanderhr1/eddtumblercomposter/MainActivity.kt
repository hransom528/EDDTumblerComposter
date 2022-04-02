package com.commanderhr1.eddtumblercomposter

//import android.Manifest
//import android.app.Activity

//import android.bluetooth.le.ScanCallback
//import android.os.Handler
//import android.os.Looper

import android.Manifest.permission.BLUETOOTH_CONNECT
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
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import java.util.*

// MainActivity
class MainActivity : AppCompatActivity() {
    //
    private val LOG_TAG : String = "AppMain"

    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Logging and Alerts

        // UI
        val btnRequestWifiPerms : Button = findViewById(R.id.btnRequestWifiPerms)
        val btnRequestBTPerms : Button = findViewById(R.id.btnRequestBTPerms)
        val btnWifiConnect: Button = findViewById(R.id.btnWifiConnect)
        val swtWifiOn : SwitchCompat = findViewById(R.id.swtWifiOn)
        val swtBTOn : SwitchCompat = findViewById(R.id.swtBTOn)


        // BLE
        /*protected val bleScanner = bluetoothAdapter.bluetoothLeScanner
        private var scanning = false
        private val SCAN_PERIOD: Long = 10000
        private var handler = Handler(Looper.getMainLooper())*/

        // WiFi initialization
        val wifi : WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // Bluetooth initialization
        val bluetoothManager : BluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter : BluetoothAdapter? = bluetoothManager.adapter
        val REQUEST_ENABLE_BT = 1
        val APP_UUID = UUID.fromString("7f27fb35-8295-4755-8596-236b479f1ff0")


        // TODO: Update Bluetooth code with server functionality
        if (bluetoothAdapter == null) {
            // Device doesn't support bluetooth
            Log.e(LOG_TAG, "Device doesn't support bluetooth")
        }

        // Request Bluetooth permission if not enabled
        checkBluetoothConnectPermission(BLUETOOTH_CONNECT)
        if (!bluetoothAdapter?.isEnabled!!) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        // Checks BLUETOOTH_CONNECT permission for Bluetooth
        checkBluetoothConnectPermission(BLUETOOTH_CONNECT)

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
        val requestCode = 1
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(
            BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        startActivityForResult(discoverableIntent, requestCode)

        // Starts bluetooth scanning
        bluetoothAdapter.listenUsingRfcommWithServiceRecord("EDDTumblerComposterApp", APP_UUID)

        // TODO: Update preliminary WiFi code
        //val wifiSpecifierBuilder : WifiNetworkSpecifier.Builder = WifiNetworkSpecifier.Builder
        //wifi.isWifiEnabled = true
        val easyConnect = wifi.isEasyConnectSupported

        // TODO: Implement permission checking inside button onClickListener methods
        // btnRequestWifiPerms
        btnRequestWifiPerms.setOnClickListener {

        }

        // btnRequestBTPerms
        btnRequestBTPerms.setOnClickListener {

        }

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

        // swtWiFiOn
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
    private fun checkBluetoothConnectPermission(permission : String) {
        // Checks app permissions for BLUETOOTH_CONNECT and notifies user if there is a permission mismatch
        if (ActivityCompat.checkSelfPermission(this, BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            val requestPermissionLauncher =
                registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
                    if (isGranted) {
                        // Permission is granted. Continue the action

                    } else {
                        // Explain to the user that the feature is unavailable because the
                        // features requires a permission that the user has denied. At the
                        // same time, respect the user's decision. Don't link to system
                        // settings in an effort to convince the user to change their
                        // decision.
                            // TODO: Update alertDialog for BT perms
                        val btDialogBuilder = AlertDialog.Builder(this)
                        btDialogBuilder.setMessage("First enable Bluetooth Access!")
                        val btAlertDialog = btDialogBuilder.create()
                        btAlertDialog.show()
                        Toast.makeText(this, "First enable Bluetooth Access!", Toast.LENGTH_LONG).show()
                        Log.w(LOG_TAG, "User permission for $permission not granted")
                    }
                }
            requestPermissionLauncher.launch(permission)
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
                    checkBluetoothConnectPermission(BLUETOOTH_CONNECT)
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