package com.commanderhr1.eddtumblercomposter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.bluetooth.*
import android.content.Context
import android.net.wifi.*
import android.widget.Button


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val wifi : WifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        // TODO: Update preliminary WiFi code
        //val wifiSpecifier : WifiNetworkSpecifier =   WifiNetworkSpecifier.Builder
        wifi.setWifiEnabled(true);
        val btnConnect = findViewById<Button>(R.id.btnConnect)

        // btnConnect
        btnConnect.setOnClickListener {

        }
    }



}