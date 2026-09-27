package com.jipange.hardwaremobileerp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogsActivity : AppCompatActivity() {

    private lateinit var logsRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyLogs: TextView
    private lateinit var etLogSearch: EditText
    private lateinit var btnExportLogs: MaterialButton

    private val api = ApiService.create()
    private var allLogsList = listOf<SystemLog>()
    private var filteredLogsList = listOf<SystemLog>()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)
        NavigationHelper.setupNavigation(this, R.id.nav_logs)

        logsRecyclerView = findViewById(R.id.logsRecyclerView)
        progressBar = findViewById(R.id.progressBarLogs)
        tvEmptyLogs = findViewById(R.id.tvEmptyLogs)
        etLogSearch = findViewById(R.id.etLogSearch)
        btnExportLogs = findViewById(R.id.btnExportLogs)

        logsRecyclerView.layoutManager = LinearLayoutManager(this)

        btnExportLogs.setOnClickListener { exportLogsToCsv() }

        etLogSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterLogs(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        startLogPolling()
    }

    private fun startLogPolling() {
        val runnable = object : Runnable {
            override fun run() {
                lifecycleScope.launch {
                    try {
                        val fetched = api.getAuditLogs()
                        allLogsList = fetched
                        val query = etLogSearch.text.toString().trim()
                        filterLogs(query)
                    } catch (e: Exception) {
                        // Silent retry or keep previous logs
                    }
                }
                handler.postDelayed(this, 5000)
            }
        }
        handler.post(runnable)
    }

    private fun filterLogs(query: String) {
        if (query.isEmpty()) {
            filteredLogsList = allLogsList
        } else {
            val q = query.lowercase(Locale.getDefault())
            filteredLogsList = allLogsList.filter {
                it.moduleName.lowercase(Locale.getDefault()).contains(q) ||
                it.actionType.lowercase(Locale.getDefault()).contains(q) ||
                it.username.lowercase(Locale.getDefault()).contains(q) ||
                it.description.lowercase(Locale.getDefault()).contains(q)
            }
        }

        if (filteredLogsList.isEmpty()) {
            tvEmptyLogs.visibility = View.VISIBLE
            logsRecyclerView.visibility = View.GONE
        } else {
            tvEmptyLogs.visibility = View.GONE
            logsRecyclerView.visibility = View.VISIBLE
            val currentAdapter = logsRecyclerView.adapter as? LogAdapter
            if (currentAdapter != null) {
                currentAdapter.updateLogs(filteredLogsList)
            } else {
                logsRecyclerView.adapter = LogAdapter(filteredLogsList) { selectedLog ->
                    showLogDetailsDialog(selectedLog)
                }
            }
        }
    }

    private fun showLogDetailsDialog(log: SystemLog) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_log_detail, null)

        val tvDetailModuleBadge = dialogView.findViewById<TextView>(R.id.tvDetailModuleBadge)
        val tvDetailLogId = dialogView.findViewById<TextView>(R.id.tvDetailLogId)
        val tvDetailActionTitle = dialogView.findViewById<TextView>(R.id.tvDetailActionTitle)
        val tvDetailTimestamp = dialogView.findViewById<TextView>(R.id.tvDetailTimestamp)
        val tvDetailUser = dialogView.findViewById<TextView>(R.id.tvDetailUser)
        val tvDetailModuleName = dialogView.findViewById<TextView>(R.id.tvDetailModuleName)
        val tvDetailActionType = dialogView.findViewById<TextView>(R.id.tvDetailActionType)
        val tvDetailDescription = dialogView.findViewById<TextView>(R.id.tvDetailDescription)
        val btnCopyLog = dialogView.findViewById<MaterialButton>(R.id.btnCopyLog)
        val btnCloseDetail = dialogView.findViewById<MaterialButton>(R.id.btnCloseDetail)

        tvDetailModuleBadge.text = log.moduleName.uppercase(Locale.getDefault())
        val colorHex = LogAdapter.getModuleColorHex(log.moduleName)
        val badgeDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f
            setColor(Color.parseColor(colorHex))
        }
        tvDetailModuleBadge.background = badgeDrawable

        tvDetailLogId.text = if (log.id > 0) "LOG #${log.id}" else "SYSTEM LOG"
        tvDetailActionTitle.text = log.actionType.replace("_", " ")
        tvDetailTimestamp.text = if (log.logTime.isNotBlank()) log.logTime else "N/A"
        tvDetailUser.text = "@${log.username}"
        tvDetailModuleName.text = log.moduleName.uppercase(Locale.getDefault())
        tvDetailActionType.text = log.actionType
        tvDetailDescription.text = log.description

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        btnCopyLog.setOnClickListener {
            val copyContent = """
                LOG DETAILS:
                Log ID: #${log.id}
                Timestamp: ${log.logTime}
                User: @${log.username}
                Module: ${log.moduleName}
                Action: ${log.actionType}
                Description: ${log.description}
            """.trimIndent()

            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Log Details", copyContent)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, "Log details copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        btnCloseDetail.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun exportLogsToCsv() {
        if (allLogsList.isEmpty()) {
            Toast.makeText(this, "No logs available to export.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Audit_Logs_$timeStamp.csv"

            val exportDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: filesDir
            val file = File(exportDir, fileName)

            val writer = FileWriter(file)
            writer.append("ID,Timestamp,User,Module,Action,Description\n")

            for (log in allLogsList) {
                val cleanDesc = log.description.replace("\"", "\"\"")
                writer.append("${log.id},\"${log.logTime}\",\"${log.username}\",\"${log.moduleName}\",\"${log.actionType}\",\"$cleanDesc\"\n")
            }

            writer.flush()
            writer.close()

            val uri: Uri = FileProvider.getUriForFile(this, "$packageName.provider", file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            Toast.makeText(this, "Logs exported to ${file.name}", Toast.LENGTH_LONG).show()
            startActivity(Intent.createChooser(shareIntent, "Share / Save Audit Logs CSV"))

        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}