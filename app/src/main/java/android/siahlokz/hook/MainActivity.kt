package android.siahlokz.hook

import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    
    private var currentProcess: Process? = null
    private var isRequestRunning = false
    private lateinit var ID: String
    private lateinit var TAG: String
    private lateinit var libs: String
    private lateinit var webTime: String
    private lateinit var appDataDir: String
    private lateinit var baseAddress: String
    private lateinit var endAddress: String
    private lateinit var selectedProcess: String
    private lateinit var selectedLibrary: String
    private lateinit var outputText: TextView
    private lateinit var appName: TextView
    private lateinit var runButton: Button
    private lateinit var scrollView: ScrollView
    private lateinit var licenseKeyEditText: TextInputEditText
    private lateinit var scanCountEditText: TextInputEditText
    private lateinit var scanDelayEditText: TextInputEditText
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var togglePassword: ImageView
    private lateinit var outputSpinnerLibs: Spinner
    private lateinit var outputSpinnerPids: Spinner
    private lateinit var outputSpinnerMethod: Spinner
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    val filteredWordsTable = listOf(
            "android.process.",
            "com.android.", 
            "com.google.", 
            "com.samsung.",
            "com.sec.",
            "media."
        )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        outputText = findViewById(R.id.output_text)
        runButton = findViewById(R.id.run_button)
        appName = findViewById(R.id.app_name)
        scrollView = findViewById(R.id.scroll_view) 
        licenseKeyEditText = findViewById(R.id.license_key)
        scanCountEditText = findViewById(R.id.scan_count)
        scanDelayEditText = findViewById(R.id.scan_delay)
        togglePassword = findViewById(R.id.toggle_password)
        outputSpinnerPids = findViewById(R.id.outputSpinnerPids)
        outputSpinnerLibs = findViewById(R.id.outputSpinnerLibs)
        outputSpinnerMethod = findViewById(R.id.outputSpinnerMethod)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        ID = Settings.Secure.getString(this@MainActivity.contentResolver, Settings.Secure.ANDROID_ID)
        appDataDir = "${applicationContext.filesDir.absolutePath}/.thumbcache"
        TAG = "@xinpochloki" //Secret Key Between CPP
        appName.text = "XV-Siahz色情片"
        
        val imageView: ImageView = findViewById(R.id.app_banner)
        Glide.with(this@MainActivity)
            .asGif()
            .load(R.drawable.hookerelmofire)
            .into(imageView)

        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val savedLicenseKey = sharedPreferences.getString("license_key", "")
        licenseKeyEditText.setText(savedLicenseKey)
        
        togglePassword.setOnClickListener {
            if (licenseKeyEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                licenseKeyEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_visibility_off)
            } else {
                licenseKeyEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_visibility_on)
            }
            licenseKeyEditText.setSelection(licenseKeyEditText.text?.length ?: 0)
        }
        
        togglePassword.setOnLongClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", ID)
            clipboard.setPrimaryClip(clip)
            outputText.append("\n\nDevice ID copied to clipboard!\n\n")
            true
        }
        
        if (DeviceUtils().isInVirtualApp(this@MainActivity) || DeviceUtils().checkIfDeviceIsRooted()) {
            fetchUnixTimestamp()
            readyHookFile { result ->
                if (result) {
                    val processRetriever = ProcessRetriever(this@MainActivity)             
                    try {
                        val pids = processRetriever.getRunningProcess()
                        val pidList = pids.split("\n")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .filter { pid ->
                                !filteredWordsTable.any { pid.startsWith(it) }
                            }
                        val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, pidList)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        outputSpinnerPids.adapter = adapter
                        deleteThumbCache()
                    } catch (e: Exception) {
                        outputText.text = "Error executing command: ${e.message}"
                    }
                }
            }
            
            swipeRefreshLayout.setOnRefreshListener {
                swipeRefreshLayout.isRefreshing = true
                fetchUnixTimestamp()
                readyHookFile { result ->
                    if (result) {
                        val processRetriever = ProcessRetriever(this@MainActivity)             
                        try {
                            val pids = processRetriever.getRunningProcess()
                            val pidList = pids.split("\n")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .filter { pid ->
                                !filteredWordsTable.any { pid.startsWith(it) }
                            }
                            val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, pidList)
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            outputSpinnerPids.adapter = adapter
                            deleteThumbCache()
                        } catch (e: Exception) {
                            outputText.text = "Error executing command: ${e.message}"
                        }
                    }
                    swipeRefreshLayout.isRefreshing = false
                }
            }
    
            outputSpinnerPids.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedProcess = parent?.getItemAtPosition(position).toString()
                    readyHookFile { result ->
                        if (result) {
                            val libRetriever = LibRetriever(this@MainActivity)
                            try {
                                libs = libRetriever.getRunningProcessLibs(selectedProcess)
                                val jsonObject = JSONObject(libs)
                
                                if (jsonObject.length() == 0) {
                                    val errorMessage = "No shared libraries found"
                                    val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, listOf(errorMessage))
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    outputSpinnerLibs.adapter = adapter
                                } else {
                                    val libKeys = jsonObject.keys().asSequence().toList()
                                    val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, libKeys)
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    outputSpinnerLibs.adapter = adapter
                                }
                
                                deleteThumbCache()
                            } catch (e: Exception) {
                                outputText.text = "Error executing command: ${e.message}"
                                val errorMessage = "Error retrieving libraries"
                                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, listOf(errorMessage))
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                outputSpinnerLibs.adapter = adapter
                            }
                        }
                    }    
                }
            
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
            
            outputSpinnerLibs.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedLibraryOrig = parent?.getItemAtPosition(position)?.toString()
                    if (selectedLibraryOrig == null) {
                        outputText.text = "Error: Invalid selection"
                        return
                    }
            
                    selectedLibrary = selectedLibraryOrig.replace(Regex(" \\d+$"), "")
            
                    if (libs.isNullOrEmpty()) {
                        outputText.text = "No shared libraries found"
                        return
                    }
                    
                    if (selectedLibrary != "No shared libraries found") {
                        try {
                            val jsonObject = JSONObject(libs)
                            if (jsonObject.has(selectedLibraryOrig)) {
                                val libraryDetails = jsonObject.getJSONObject(selectedLibraryOrig)
                                baseAddress = libraryDetails.getString("Base Address")
                                endAddress = libraryDetails.getString("End Address")
                                try {
                                    val sizeLib = calculateSize(baseAddress, endAddress)
                                    runOnUiThread {
                                        outputText.text = "Process: $selectedProcess\nLibrary: $selectedLibraryOrig\nSize: $sizeLib\nBase Address: $baseAddress\nEnd Address: $endAddress"
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    runOnUiThread {
                                        outputText.text = "Error calculating size: ${e.message}"
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    outputText.text = "No shared libraries found"
                                }
                            }
                        } catch (e: JSONException) {
                            runOnUiThread {
                                outputText.text = "No shared libraries found"
                            }
                        }
                    }
                }
            
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
            
            runButton.setOnClickListener {
                val licenseKey = licenseKeyEditText.text.toString()
                val scanCount = scanCountEditText.text.toString()
                val scanDelay = scanDelayEditText.text.toString()
                
                if (selectedProcess.isEmpty()) {
                    outputText.text = "No selected process"
                    return@setOnClickListener
                }
                
                if (selectedLibrary == "No shared libraries found") {
                    outputText.text = "No selected library"
                    return@setOnClickListener
                }
                
                if (scanCount.isEmpty()) {
                    scanCountEditText.error = "Scan count is required"
                    return@setOnClickListener
                }
    
                if (scanDelay.isEmpty()) {
                    scanDelayEditText.error = "Scan delay is required"
                    return@setOnClickListener
                }
                
                if (licenseKey.isEmpty()) {
                    licenseKeyEditText.error = "License key is required"
                    return@setOnClickListener
                }
                
                with(sharedPreferences.edit()) {
                    putString("license_key", licenseKey)
                    apply()
                }
    
                if (currentProcess != null) {
                    currentProcess?.destroy()
                    currentProcess = null
                    runButton.text = "RUN"
                    
                    itemsState(true)
                    
                    return@setOnClickListener
                }
            
                runButton.text = "STOP"
                outputText.text = null
                itemsState(false)
                readyHookFile { result ->
                    if (result) {
                        val command = if (DeviceUtils().checkIfDeviceIsRooted()) {
                            "su -c chmod 7777 $appDataDir/thumbcache_idx1 && su -c $appDataDir/thumbcache_idx1 $scanCount $scanDelay $selectedProcess $selectedLibrary $baseAddress $endAddress $licenseKey $TAG $ID $webTime"
                        } else {
                            "chmod 7777 $appDataDir/thumbcache_idx1 && $appDataDir/thumbcache_idx1 $scanCount $scanDelay $selectedProcess $selectedLibrary $baseAddress $endAddress $licenseKey $TAG $ID $webTime"
                        }
                        executeCommand(command)
                    } else {
                        Toast.makeText(this@MainActivity, "Something went wrong!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } 
    }

    private fun readyHookFile(callback: (Boolean) -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        val outputDirectory = File(appDataDir)
    
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }
    
        handler.postDelayed({
            val assetExtractor = AssetExtractor(this@MainActivity)
            assetExtractor.extractAssetFile("thumbcache_idx", ".thumbcache")
    
            val zipExtractor = ZipExtractor(this@MainActivity)
            val result = zipExtractor.unzip("$appDataDir/thumbcache_idx", "@xinpochloki", appDataDir)
    
            callback(result)
        }, 1000)
    }

    private fun executeCommand(command: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                currentProcess = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                val reader = BufferedReader(InputStreamReader(currentProcess!!.inputStream))
                val errorReader = BufferedReader(InputStreamReader(currentProcess!!.errorStream))
    
                var line: String?
    
                while (reader.readLine().also { line = it } != null) {
                    withContext(Dispatchers.Main) {
                        outputText.append(line + "\n")
                        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
                    }
                }
    
                while (errorReader.readLine().also { line = it } != null) {
                    withContext(Dispatchers.Main) {
                        outputText.append("Something went wrong!\n")
                        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
                    }
                }
    
                reader.close()
                errorReader.close()
    
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    outputText.append("\nScanning stopped.")
                }
            } finally {
                deleteThumbCache()
                currentProcess = null
                runButton.text = "RUN"
                withContext(Dispatchers.Main) {
                    itemsState(true)
                }
            }
        }
    }
    
    private fun fetchUnixTimestamp() {
        if (isRequestRunning) {
            return
        }
        isRequestRunning = true
        runButton.isEnabled = false
        Thread {
            try {
                val unixTimeFetcher = UnixTimeFetcher()
                webTime = unixTimeFetcher.getUnixTimestamp() ?: "Failed to fetch time"
                runOnUiThread {
                    runButton.isEnabled = true
                    isRequestRunning = false
                }
            } catch (e: Exception) {
                runOnUiThread {
                    outputText.text = "Error: ${e.message}"
                    runButton.isEnabled = true
                    isRequestRunning = false
                }
            }
        }.start()
    }
    
    private fun itemsState(state: Boolean) {
        if (state) {
            outputSpinnerPids.isEnabled = true
            outputSpinnerLibs.isEnabled = true
            outputSpinnerMethod.isEnabled = true
            licenseKeyEditText.isEnabled = true
            scanCountEditText.isEnabled = true
            scanDelayEditText.isEnabled = true
            togglePassword.isEnabled = true
        } else {
            outputSpinnerPids.isEnabled = false
            outputSpinnerLibs.isEnabled = false
            outputSpinnerMethod.isEnabled = false
            licenseKeyEditText.isEnabled = false
            scanCountEditText.isEnabled = false
            scanDelayEditText.isEnabled = false
            togglePassword.isEnabled = false
        }
    }
    
    private fun calculateSize(baseAddress: String?, endAddress: String?): String {
        return try {
            if (baseAddress == null || endAddress == null) return "Invalid addresses"
            val base = baseAddress.removePrefix("0x").toLong(16)
            val end = endAddress.removePrefix("0x").toLong(16)
            val sizeInBytes = if (end > base) end - base else 0L
            when {
                sizeInBytes >= 1024 * 1024 * 1024 -> "%.2f GB".format(sizeInBytes / (1024.0 * 1024 * 1024))
                sizeInBytes >= 1024 * 1024 -> "%.2f MB".format(sizeInBytes / (1024.0 * 1024))
                sizeInBytes >= 1024 -> "%.2f KB".format(sizeInBytes / 1024.0)
                else -> "$sizeInBytes Bytes"
            }
        } catch (e: Exception) {
            "Error"
        }
    }

    private fun deleteThumbCache() {
        val thumbCacheDir = File(appDataDir)
        if (thumbCacheDir.exists()) {
            thumbCacheDir.deleteRecursively()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentProcess?.destroy()
        currentProcess = null
        deleteThumbCache()
    }
    
}