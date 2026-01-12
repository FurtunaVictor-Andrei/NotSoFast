package com.example.notsofastfinal

import android.Manifest

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity


import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.*

class MainActivity : ComponentActivity() {


    private lateinit var statusText: TextView
    private lateinit var btnShow: Button
    private lateinit var listView: ListView
    private lateinit var btnFwd: Button
    private lateinit var btnBck: Button
    private lateinit var btnLft: Button
    private lateinit var btnRight: Button


    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var btSocket: BluetoothSocket? = null
    private val mDeviceList = ArrayList<String>()

    private lateinit var listAdapter: ArrayAdapter<String>
    private lateinit var logListView: ListView
    private lateinit var btnShowLogs: Button
    private val logList = ArrayList<String>()
    private lateinit var logAdapter: ArrayAdapter<String>
    private var isListening = false


    private val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    companion object {
        private const val REQUEST_PERMISSION_BT = 101
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.applayout)


        statusText = findViewById(R.id.tvStatus)
        btnShow = findViewById(R.id.btnShowDevices)
        listView = findViewById(R.id.deviceList)


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mDeviceList)
        listView.adapter = listAdapter


        btnShow.setOnClickListener {
            checkPermissionsAndShowDevices()
        }


        listView.setOnItemClickListener { _, _, position, _ ->
            val info = mDeviceList[position]

            val address = info.substring(info.length - 17)
            statusText.text = "Connecting..."


            Thread {
                connectToDevice(address)
            }.start()
        }
        btnFwd = findViewById(R.id.btnForward)
        btnBck = findViewById(R.id.btnBackward)
        btnLft = findViewById(R.id.btnLeft)
        btnRight=findViewById(R.id.btnRight)
        logListView = findViewById(R.id.listViewLogs)
        btnShowLogs = findViewById(R.id.btnShowLogs)

        logAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, logList)
        logListView.adapter = logAdapter


        btnShowLogs.setOnClickListener {
            if (logListView.visibility == android.view.View.GONE) {
                logListView.visibility = android.view.View.VISIBLE
                btnFwd.visibility = android.view.View.GONE
                btnBck.visibility = android.view.View.GONE
                btnShow.visibility = android.view.View.GONE 
                statusText.visibility = android.view.View.GONE
                listView.visibility = android.view.View.GONE
                btnShowLogs.text = "Show logs"
            } else {
                logListView.visibility = android.view.View.GONE
                btnFwd.visibility = android.view.View.VISIBLE
                btnBck.visibility = android.view.View.VISIBLE
                btnShow.visibility = android.view.View.VISIBLE
                statusText.visibility = android.view.View.VISIBLE
                btnShowLogs.text = "Hide logs"
            }
        }


        btnFwd.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.isPressed = true
                    sendCommand("F")
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.isPressed = false
                    sendCommand("S")
                    view.performClick()
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    view.isPressed = false
                    sendCommand("S")
                    true
                }
                else -> false
            }
        }


        btnBck.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.isPressed = true
                    sendCommand("B")
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.isPressed = false
                    sendCommand("S")
                    view.performClick()
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    view.isPressed = false
                    sendCommand("S")
                    true
                }
                else -> false
            }
        }

        btnLft.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.isPressed = true
                    sendCommand("L")
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.isPressed = false
                    sendCommand("S")
                    view.performClick()
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    view.isPressed = false
                    sendCommand("S")
                    true
                }
                else -> false
            }
        }
        btnRight.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.isPressed = true
                    sendCommand("R")
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.isPressed = false
                    sendCommand("S")
                    view.performClick()
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    view.isPressed = false
                    sendCommand("S")
                    true
                }
                else -> false
            }
        }
    }

    private fun checkPermissionsAndShowDevices() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            return
        }


        if (!mBluetoothAdapter!!.isEnabled) {
            Toast.makeText(this, "Turn bluetooth on!", Toast.LENGTH_SHORT).show()
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_PERMISSION_BT)
                return
            }
            startActivity(enableBtIntent)
            return
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_PERMISSION_BT)
                return
            }
        }

        showPairedDevices()
    }

    private fun showPairedDevices() {

        try {
            val pairedDevices: Set<BluetoothDevice>? = mBluetoothAdapter?.bondedDevices
            mDeviceList.clear()

            if (pairedDevices != null && pairedDevices.isNotEmpty()) {
                for (device in pairedDevices) {

                    mDeviceList.add("${device.name}\n${device.address}")
                }
            } else {
                Toast.makeText(this, "No device found", Toast.LENGTH_SHORT).show()
            }
            listAdapter.notifyDataSetChanged()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    private fun listenForData() {
        val inputStream = btSocket?.inputStream
        val buffer = ByteArray(1024)
        isListening = true

        Thread {
            while (isListening && btSocket != null && btSocket!!.isConnected) {
                try {
                    val bytesCount = inputStream?.read(buffer)
                    if (bytesCount != null && bytesCount > 0) {
                        val receivedData = String(buffer, 0, bytesCount)


                        if (receivedData.contains("T")) {
                            runOnUiThread {
                                addLogEntry()
                            }
                        }
                    }
                } catch (e: IOException) {
                    isListening = false
                    break
                }
            }
        }.start()
    }

    private fun addLogEntry() {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
        val currentTime = sdf.format(java.util.Date())
        val message = "Autobreaking used at $currentTime"

        logList.add(0, message)
        logAdapter.notifyDataSetChanged()

    }
    private fun connectToDevice(macAddress: String) {
        try {
            if (btSocket != null && btSocket!!.isConnected) {
                btSocket!!.close()
            }

            val device = mBluetoothAdapter!!.getRemoteDevice(macAddress)


            try {
                btSocket = device.createRfcommSocketToServiceRecord(myUUID)
            } catch (e: SecurityException) {
                e.printStackTrace()
                return
            }


            try { mBluetoothAdapter!!.cancelDiscovery() } catch(e: SecurityException) {}


            try {
                btSocket!!.connect()


                runOnUiThread {
                    statusText.text = "Connected to $macAddress"
                    Toast.makeText(this, "Succesfully connected!", Toast.LENGTH_SHORT).show()
                    listView.visibility = android.view.View.GONE
                    listenForData()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    statusText.text = "Connection error"
                    Toast.makeText(this, "Connection error", Toast.LENGTH_SHORT).show()
                }
                try {
                    btSocket!!.close()
                } catch (e2: IOException) {
                    e2.printStackTrace()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun sendCommand(cmd: String) {
        Thread {
            if (btSocket != null && btSocket!!.isConnected) {
                try {
                    btSocket!!.outputStream.write(cmd.toByteArray())
                    btSocket!!.outputStream.flush()


                } catch (e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this, "Command sending error", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Bluetooth not connected", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }


}
