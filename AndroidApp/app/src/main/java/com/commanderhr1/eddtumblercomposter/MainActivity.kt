package com.commanderhr1.eddtumblercomposter

//import android.Manifest
//import android.app.Activity

import android.Manifest.permission.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.*
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.net.wifi.WifiNetworkSuggestion
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import java.io.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket


// MainActivity
class MainActivity : AppCompatActivity() {
    // Logging
    private val LOG_TAG : String = "AppMain"

    // WiFi
    private var socket : Socket? = null
    private var wifi : WifiManager? = null
    private val networkSSID = "Meir1"
    private val networkPass = "Harran!404"
    //private lateinit var networkInfo : NetworkInfo
    private var cm : ConnectivityManager? = null
    private lateinit var networkRequest : NetworkRequest

    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI
        val btnRequestWifiPerms : Button = findViewById(R.id.btnRequestWifiPerms)
        val btnWifiConnect: Button = findViewById(R.id.btnWifiConnect)
        val swtWifiOn : SwitchCompat = findViewById(R.id.swtWifiOn)
        val toolbar : Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        // TODO: Update toolbar

        // WiFi initialization
        checkWifiPermission(ACCESS_FINE_LOCATION)
        wifi = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // TODO: Update WiFi connection code
        // NetworkRequest implementation
        val wifiSpecifierBuilder : WifiNetworkSpecifier.Builder = WifiNetworkSpecifier.Builder().setSsid(networkSSID)
        wifiSpecifierBuilder.setWpa2Passphrase(networkPass)
        val wifiSpecifier : WifiNetworkSpecifier = wifiSpecifierBuilder.build()
        val networkRequestBuilder = NetworkRequest.Builder()
        networkRequestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        networkRequestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
        networkRequestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
        networkRequestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        networkRequestBuilder.setNetworkSpecifier(wifiSpecifier)
        networkRequest = networkRequestBuilder.build()
        cm = this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        // WiFi Scanning
        val wifiIntentFilter = IntentFilter()
        wifiIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        applicationContext.registerReceiver(wifiScanReceiver, wifiIntentFilter)

        // TCP client connection
        val ipAddr = "192.168.4.1"
        val wifiPort = 80
        //var tcpClient : TcpClient

        // btnRequestWifiPerms
        btnRequestWifiPerms.setOnClickListener {
            checkWifiPermission(ACCESS_FINE_LOCATION)
            checkWifiPermission(ACCESS_COARSE_LOCATION)
            //checkWifiPermission(CHANGE_NETWORK_STATE)
            //checkWifiPermission(WRITE_SETTINGS)
            // WifiNetworkSuggestion implementation
            val wifiNetworkSuggestionBuilder : WifiNetworkSuggestion.Builder = WifiNetworkSuggestion.Builder()
            wifiNetworkSuggestionBuilder.setSsid(networkSSID)
            //wifiNetworkSuggestionBuilder.setIsHiddenSsid(wifiConf.hiddenSSID)
            wifiNetworkSuggestionBuilder.setWpa2Passphrase(networkPass)
            wifiNetworkSuggestionBuilder.setPriority(999)

            val wifiNetworkSuggestion : WifiNetworkSuggestion = wifiNetworkSuggestionBuilder.build()
            val suggestionList : MutableList<WifiNetworkSuggestion> = ArrayList()
            suggestionList.add(wifiNetworkSuggestion)
            val suggestionStatus : Int = wifi!!.addNetworkSuggestions(suggestionList)
            if (suggestionStatus ) {

            }
        }

        // btnConnect
        btnWifiConnect.setOnClickListener {
            // Starts WiFi scanning
            val wifiScanSuccess : Boolean = wifi!!.startScan()
            if (!wifiScanSuccess) {
                scanFailure()
            }

            // TODO: Implement WiFi connection in separate thread
            val isConnected = wifiConnectComposter(networkRequest)
            if (isConnected) {
                socket = Socket(ipAddr, wifiPort)
                val socketAddress = InetSocketAddress(wifiPort)
                socket!!.connect(socketAddress)
                //val wifiTcpThread = tcpThread(true, false, null, "tcpThread", 1, )
            }
        }

        // swtWiFiOn
        swtWifiOn.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                wifi!!.isWifiEnabled = false
                wifi!!.disconnect()
            }
            else {
                wifi!!.isWifiEnabled = true
            }
        }
    }

    // onStart
    override fun onStart() {
        super.onStart()

        // Gets configured WiFi networks
        checkWifiPermission(ACCESS_COARSE_LOCATION)
        checkWifiPermission(ACCESS_FINE_LOCATION)
    }

    // onDestroy
    override fun onDestroy() {
        super.onDestroy()

        // Disconnects WiFi
        cm?.unregisterNetworkCallback(wifiNetworkCallback)

        // Closes TCP socket
        socket?.close()

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(wifiScanReceiver)
    }

    // onResume
    override fun onResume() {
        super.onResume()
        checkWifiPermission(ACCESS_FINE_LOCATION)
        val isConnected : Boolean = isConnected(applicationContext, ConnectivityManager.TYPE_WIFI)
    }

    // Checks for BLUETOOTH_CONNECT permission
    private fun checkWifiPermission(permission : String) {
        // Checks app permissions for BLUETOOTH_CONNECT and notifies user if there is a permission mismatch
        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
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
                        // TODO: Update alertDialog for WiFi perms
                        val wifiDialogBuilder = AlertDialog.Builder(this)
                        wifiDialogBuilder.setMessage("First enable Wifi permissions!")
                        val wifiAlertDialog = wifiDialogBuilder.create()
                        wifiAlertDialog.show()
                        Toast.makeText(this, "First enable Wifi permissions!", Toast.LENGTH_LONG).show()
                        Log.w(LOG_TAG, "User permission for $permission not granted")
                    }
                }
            requestPermissionLauncher.launch(permission)
        }
    }

    // Checks if network is connected
    private fun isConnected(context: Context, networkType: Int): Boolean {
        val nw : Network? = cm!!.activeNetwork ?: return false
        val nwCapabilities = cm!!.getNetworkCapabilities(nw) ?: return false
        return when {
            nwCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            else -> false
        }
        Log.i(LOG_TAG, "Network Connected: $isConnected")
    }

    // Creates a BroadcastReceiver for WiFi scan.
    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                scanSuccess()
            } else {
                scanFailure()
            }
        }
    }

    // WiFi scan successful
    private fun scanSuccess() {
        val results = wifi?.scanResults

        Toast.makeText(applicationContext, "WiFi Networks Successfully Scanned", Toast.LENGTH_SHORT).show()
        Log.i(LOG_TAG, "WiFi Networks Successfully Scanned")


        //... use new scan results ...
        Log.i(LOG_TAG, "Networks: ")
        for (result in results!!) {
            Log.i(LOG_TAG, "\t$result")
            if (result.SSID.equals(networkSSID)) {
                Log.i(LOG_TAG, "$networkSSID Detected")
                wifiConnectComposter(networkRequest)
            }
        }
    }

    // WiFi scan failed
    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        val results = wifi?.scanResults

        Toast.makeText(applicationContext, "WiFi Network Scan Unsuccessful", Toast.LENGTH_SHORT).show()
        Log.e(LOG_TAG, "WiFi Network Scan Unsuccessful")
        //... potentially use older scan results ...
    }

    // ConnectivityManager network callback for WiFi connection
    // TODO: Update wifiNetworkCallback
    private val wifiNetworkCallback = object : ConnectivityManager.NetworkCallback()  {
        override fun onAvailable(network : Network) {
            super.onAvailable(network)
            Log.i(LOG_TAG, "WifiNetworkCallback onAvailable")
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.i(LOG_TAG, "WifiNetworkCallback onLost")
        }
    }

    // Connect to composter
    private fun wifiConnectComposter(networkRequest : NetworkRequest): Boolean {
        cm?.registerNetworkCallback(networkRequest, wifiNetworkCallback)
        cm?.requestNetwork(networkRequest, wifiNetworkCallback)
        return isConnected(applicationContext, ConnectivityManager.TYPE_WIFI)
    }

    /*
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
            val TAG: String? = TcpClient::class.simpleName
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

    // TODO: Update TCP thread
    // Thread for handling TCP I/O
    fun tcpThread(
        start: Boolean = true,
        isDaemon: Boolean = false,
        contextClassLoader: ClassLoader? = null,
        name: String? = null,
        priority: Int = -1,
        block: () -> Unit,
    ) {
        val socketInputStream : InputStream = socket!!.getInputStream()
        var socketOutputStream : OutputStream = socket!!.getOutputStream()

        // Data available on TCP socket
        if (socketInputStream.available() > 0) {
            val inputString : String = socketInputStream.read().toString()
            Log.i(LOG_TAG, "Received TCP data: $inputString")
        }
    }*/
}