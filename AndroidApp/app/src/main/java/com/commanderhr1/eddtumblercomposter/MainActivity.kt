@file:Suppress("DEPRECATION")

package com.commanderhr1.eddtumblercomposter

//import android.Manifest
//import android.app.Activity

//import android.bluetooth.le.ScanCallback

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.*
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.net.wifi.WifiNetworkSuggestion
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import java.io.*
import java.net.InetAddress
import java.net.Socket
import java.util.*

// MainActivity
class MainActivity : AppCompatActivity() {
    // Logging
    private val LOG_TAG : String = "AppMain"

    // Bluetooth
    private lateinit var bluetoothManager : BluetoothManager
    private lateinit var bluetoothAdapter : BluetoothAdapter
    val APP_UUID: UUID = UUID.fromString("7f27fb35-8295-4755-8596-236b479f1ff0")

    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI
        val btnRequestWifiPerms : Button = findViewById(R.id.btnRequestWifiPerms)
        val btnRequestBTPerms : Button = findViewById(R.id.btnRequestBTPerms)
        val btnWifiConnect: Button = findViewById(R.id.btnWifiConnect)
        val swtWifiOn : SwitchCompat = findViewById(R.id.swtWifiOn)
        val swtBTOn : SwitchCompat = findViewById(R.id.swtBTOn)

        // Shared preferences
        val examplePrefs = getSharedPreferences("PREFS", 0)
        val editor = examplePrefs.edit()

        // BLE
        /*protected val bleScanner = bluetoothAdapter.bluetoothLeScanner
        private var scanning = false
        private val SCAN_PERIOD: Long = 10000
        private var handler = Handler(Looper.getMainLooper())*/

        // WiFi initialization
        val wifi : WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiList = wifi.configuredNetworks
        val networkSSID = "EDDTumblerComposter"
        val networkPass = "12345678"

        // WiFi Direct
        val looper : Looper = Looper.getMainLooper()
        val wifiP2P : WifiP2pManager = applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        val p2pChannelListener : WifiP2pManager.ChannelListener = WifiP2pManager.ChannelListener {
            // TODO: Update WiFi direct with ChannelListener and BroadcastReceiver
        }
        val wifiP2PChannel : WifiP2pManager.Channel =  wifiP2P.initialize(applicationContext, looper, p2pChannelListener)


        // Bluetooth initialization
        bluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        val REQUEST_ENABLE_BT = 1

        // Checks BLUETOOTH_CONNECT permission for Bluetooth
        checkBluetoothConnectPermission(BLUETOOTH_CONNECT)

        // Queries list of previously paired devices
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC Address

            // Bypass scanning, go right to connection
            if (deviceName.equals("EDDTumblerComposter")) {
                // TODO: Implement scanning bypass
            }
        }


        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

        // Starts bluetooth scanning
        //AcceptThread().run()


        // TODO: Update WiFi scanning and connection code

        val wifiSpecifierBuilder : WifiNetworkSpecifier.Builder = WifiNetworkSpecifier.Builder()
        wifiSpecifierBuilder.setSsid(networkSSID)
        //wifiSpecifierBuilder.setWpa2Passphrase(networkPass)
        val wifiSpecifier : WifiNetworkSpecifier = wifiSpecifierBuilder.build()

        // WifiConfiguration implementation, deprecated but should work
        val wifiConf : WifiConfiguration = WifiConfiguration()
        wifiConf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
        wifiConf.preSharedKey = "\""+ networkPass +"\"";
        wifi.addNetwork(wifiConf)
        for (i in wifiList) {
            if (i.SSID != null && i.SSID == "\"" + networkSSID + "\"") {
                wifi.disconnect()
                wifi.enableNetwork(i.networkId, true)
                wifi.reconnect()
                break
            }
        }

        // WifiNetworkSuggestion implementation
        val wifiNetworkSuggestionBuilder : WifiNetworkSuggestion.Builder = WifiNetworkSuggestion.Builder()
        wifiNetworkSuggestionBuilder.setSsid(networkSSID)
        //wifiNetworkSuggestionBuilder.setWpa2Passphrase(networkPass)
        val wifiNetworkSuggestion : WifiNetworkSuggestion = wifiNetworkSuggestionBuilder.build()

        // NetworkRequest implementation
        val networkRequestBuilder = NetworkRequest.Builder()
        networkRequestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        networkRequestBuilder.setNetworkSpecifier(wifiSpecifier)
        val networkRequest = networkRequestBuilder.build()
        val cm : ConnectivityManager = this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network : Network) {
                Log.e(LOG_TAG, "The default network is now: $network")
            }

            override fun onLost(network : Network) {
                Log.e(LOG_TAG,
                    "The application no longer has a default network. The last default network was $network"
                )
            }

            override fun onCapabilitiesChanged(network : Network, networkCapabilities : NetworkCapabilities) {
                Log.e(LOG_TAG, "The default network changed capabilities: $networkCapabilities")
            }

            override fun onLinkPropertiesChanged(network : Network, linkProperties : LinkProperties) {
                Log.e(LOG_TAG, "The default network changed link properties: $linkProperties")
            }
        })
        wifi.addNetwork(wifiConf)

        val easyConnect = wifi.isEasyConnectSupported
        val wifiIntentFilter = IntentFilter()
        wifiIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        applicationContext.registerReceiver(wifiScanReceiver, wifiIntentFilter)

        // Starts WiFi scanning
        var wifiScanSuccess : Boolean = wifi.startScan()
        if (!wifiScanSuccess) {
            scanFailure(wifi)
        }
        else {
            scanSuccess(wifi)
        }

        // TCP client connection
        var tcpClient : TcpClient

        // TODO: Implement permission checking inside button onClickListener methods
        // btnRequestWifiPerms
        btnRequestWifiPerms.setOnClickListener {

        }

        // btnRequestBTPerms
        btnRequestBTPerms.setOnClickListener {
            // Request BLUETOOTH_CONNECT permission
            checkBluetoothConnectPermission(BLUETOOTH_CONNECT)
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }

            // Enables Bluetooth discoverability
            val requestCode = 1
            val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(
                BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            startActivityForResult(discoverableIntent, requestCode)
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
            if (!isChecked) {
                wifi.disconnect()
            }
            else {

            }
        }
    }

    // onDestroy
    override fun onDestroy() {
        super.onDestroy()

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
        unregisterReceiver(wifiScanReceiver)
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

    // Creates a BroadcastReceiver for ACTION_FOUND.
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

    // Creates a BroadcastReceiver for WiFi scan.
    private val wifiScanReceiver = object : BroadcastReceiver() {
        val wifi : WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                scanSuccess(wifi)
            } else {
                scanFailure(wifi)
            }
        }
    }

    // TODO: Update Bluetooth server functionality
    // Bluetooth connection/scanning thread
    /*
    @SuppressLint("MissingPermission")
    private inner class AcceptThread : Thread() {
        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("EDDTumblerComposterApp", APP_UUID)
        }

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(LOG_TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    //manageMyConnectedSocket(it)
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "Could not close the connect socket", e)
            }
        }
    }*/

    /*
    // Bluetooth service for composter
    class BluetoothService(
        // handler that gets info from Bluetooth service
        private val handler: Handler,
        val TAG : String
    ) {
        private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
            private val mmInStream: InputStream = mmSocket.inputStream
            private val mmOutStream: OutputStream = mmSocket.outputStream
            private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

            // Defines several constants used when transmitting messages between the
            // service and the UI.
            val MESSAGE_READ: Int = 0
            val MESSAGE_WRITE: Int = 1
            val MESSAGE_TOAST: Int = 2

            override fun run() {
                var numBytes: Int // bytes returned from read()

                // Keep listening to the InputStream until an exception occurs.
                while (true) {
                    // Read from the InputStream.
                    numBytes = try {
                        mmInStream.read(mmBuffer)
                    } catch (e: IOException) {
                        Log.d(TAG, "Input stream was disconnected", e)
                        break
                    }

                    // Send the obtained bytes to the UI activity.
                    val readMsg = handler.obtainMessage(
                        MESSAGE_READ, numBytes, -1,
                        mmBuffer)
                    readMsg.sendToTarget()
                }
            }

            // Call this from the main activity to send data to the remote device.
            fun write(bytes: ByteArray) {
                try {
                    mmOutStream.write(bytes)
                } catch (e: IOException) {
                    Log.e(TAG, "Error occurred when sending data", e)

                    // Send a failure message back to the activity.
                    val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
                    val bundle = Bundle().apply {
                        putString("toast", "Couldn't send data to the other device")
                    }
                    writeErrorMsg.data = bundle
                    handler.sendMessage(writeErrorMsg)
                    return
                }

                // Share the sent message with the UI activity.
                val writtenMsg = handler.obtainMessage(
                    MESSAGE_WRITE, -1, -1, mmBuffer)
                writtenMsg.sendToTarget()
            }

            // Call this method from the main activity to shut down the connection.
            fun cancel() {
                try {
                    mmSocket.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Could not close the connect socket", e)
                }
            }
        }
    }*/


    // WiFi scan successful
    private fun scanSuccess(wifiManager : WifiManager) {
        val results = wifiManager.scanResults

        //... use new scan results ...
        for (result in results) {
            if (result.SSID.equals("EDDTumblerComposter")) {
                // TODO: Implement WiFi connection
                wifiConnectComposter(wifiManager)
            }
        }
    }

    // WiFi scan failed
    private fun scanFailure(wifiManager : WifiManager) {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        val results = wifiManager.scanResults
        //... potentially use older scan results ...
    }

    // Connect to composter
    private fun wifiConnectComposter(wifiManager: WifiManager) {

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

    class TcpClient(listener: OnMessageReceived?) {
        // message to send to the server
        private var mServerMessage: String? = null

        // sends message received notifications
        private var mMessageListener: OnMessageReceived? = null

        // while this is true, the server will continue running
        private var mRun = false

        // used to send messages
        private var mBufferOut: PrintWriter? = null

        // used to read messages from the server
        private var mBufferIn: BufferedReader? = null

        /**
         * Sends the message entered by client to the server
         *
         * @param message text entered by client
         */
        fun sendMessage(message: String) {
            val runnable = Runnable {
                if (mBufferOut != null) {
                    Log.d(TAG, "Sending: $message")
                    mBufferOut!!.println(message)
                    mBufferOut!!.flush()
                }
            }
            val thread = Thread(runnable)
            thread.start()
        }

        /**
         * Close the connection and release the members
         */
        fun stopClient() {
            mRun = false
            if (mBufferOut != null) {
                mBufferOut!!.flush()
                mBufferOut!!.close()
            }
            mMessageListener = null
            mBufferIn = null
            mBufferOut = null
            mServerMessage = null
        }

        fun run() {
            mRun = true
            try {
                //here you must put your computer's IP address.
                val serverAddr: InetAddress = InetAddress.getByName(SERVER_IP)
                Log.d("TCP Client", "C: Connecting...")

                //create a socket to make the connection with the server
                val socket = Socket(serverAddr, SERVER_PORT)
                try {

                    //sends the message to the server
                    mBufferOut = PrintWriter(
                        BufferedWriter(OutputStreamWriter(socket.getOutputStream())),
                        true
                    )

                    //receives the message which the server sends back
                    mBufferIn = BufferedReader(InputStreamReader(socket.getInputStream()))


                    //in this while the client listens for the messages sent by the server
                    while (mRun) {
                        mServerMessage = mBufferIn!!.readLine()
                        if (mServerMessage != null && mMessageListener != null) {
                            //call the method messageReceived from MyActivity class
                            mMessageListener!!.messageReceived(mServerMessage)
                        }
                    }
                    Log.d("RESPONSE FROM SERVER", "S: Received Message: '$mServerMessage'")
                } catch (e: Exception) {
                    Log.e("TCP", "S: Error", e)
                } finally {
                    //the socket must be closed. It is not possible to reconnect to this socket
                    // after it is closed, which means a new socket instance has to be created.
                    socket.close()
                }
            } catch (e: Exception) {
                Log.e("TCP", "C: Error", e)
            }
        }

        //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
        //class at on AsyncTask doInBackground
        interface OnMessageReceived {
            fun messageReceived(message: String?)
        }

        companion object {
            val TAG: String = TcpClient::class.java.simpleName
            const val SERVER_IP = "192.168.1.8" //server IP address
            const val SERVER_PORT = 1234
        }

        /**
         * Constructor of the class. OnMessagedReceived listens for the messages received from server
         */
        init {
            mMessageListener = listener
        }
    }
}