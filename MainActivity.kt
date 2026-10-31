package com.potatographics.app

import android.app.Activity
import android.os.Bundle
import android.os.BatteryManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.io.File
import java.util.Locale

class MainActivity : Activity() {
    private val bg = Color.rgb(7, 11, 20)
    private val panel = Color.rgb(15, 24, 40)
    private val blue = Color.rgb(0, 190, 255)
    private val white = Color.rgb(235, 245, 255)
    private val muted = Color.rgb(145, 165, 190)
    private lateinit var content: LinearLayout
    private var selectedGame = "Free Fire"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildHome()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun text(value: String, size: Float, color: Int = white, bold: Boolean = false): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(2), dp(5), dp(2), dp(5))
        }
    }

    private fun card(title: String, detail: String): LinearLayout {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(panel)
        }
        box.addView(text(title, 16f, blue, true))
        box.addView(text(detail, 14f, white))
        val params = LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, dp(6), 0, dp(6))
        }
        content.addView(box, params)
        return box
    }

    private fun button(label: String, action: () -> Unit) {
        val b = Button(this).apply {
            text = label
            setTextColor(Color.BLACK)
            setBackgroundColor(blue)
            isAllCaps = false
            setOnClickListener { action() }
        }
        content.addView(b, LinearParams())
    }

    private fun LinearParams(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(5), 0, dp(5)) }

    private fun shell(title: String, subtitle: String) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(16), dp(18), dp(16), dp(16))
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(text("POTATO GRAPHICS", 12f, blue, true))
            addView(text("V3.0  •  ULTIMATE GAMING CONTROL CENTER", 19f, white, true))
            addView(text(title, 22f, blue, true))
            addView(text(subtitle, 13f, muted))
        }
        root.addView(header)
        val scroll = ScrollView(this)
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun buildHome() {
        shell("Dashboard", "A gaming companion for device information and settings.")
        card("SELECTED GAME", selectedGame)
        button("Choose Game") { showGames() }
        card("DEVICE SNAPSHOT", deviceSummary())
        card("BATTERY", batterySummary())
        card("STORAGE", storageSummary())
        card("GAMING NOTE", "This app provides information and guides. It does not modify games, unlock FPS, or guarantee performance improvements.")
        button("Device Monitor") { showMonitor() }
        button("Gaming Guides") { showGuides() }
        button("Touch Tap Counter") { showTapCounter() }
        button("App Information") { showAbout() }
    }

    private fun deviceSummary(): String =
        "${Build.MANUFACTURER} ${Build.MODEL}\nAndroid ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n${resources.displayMetrics.widthPixels} × ${resources.displayMetrics.heightPixels} display"

    private fun batteryIntent(): Intent? =
        registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

    private fun batterySummary(): String {
        val i = batteryIntent() ?: return "Battery information unavailable"
        val level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val pct = if (level >= 0 && scale > 0) level * 100 / scale else -1
        val temp = i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val c = if (temp >= 0) String.format(Locale.US, "%.1f°C", temp / 10f) else "Unavailable"
        return "Charge: ${if (pct >= 0) "$pct%" else "Unavailable"}\nBattery temperature: $c"
    }

    private fun storageSummary(): String {
        val path = Environment.getDataDirectory()
        val stat = android.os.StatFs(path.path)
        val total = stat.totalBytes / (1024L * 1024L * 1024L)
        val free = stat.availableBytes / (1024L * 1024L * 1024L)
        return "Internal storage: approximately ${total - free} GB used of $total GB\nAvailable: $free GB"
    }

    private fun showGames() {
        shell("Game Selector", "Choose a game to view its companion profile.")
        listOf("Free Fire", "PUBG Mobile", "Call of Duty: Mobile", "Mobile Legends").forEach { game ->
            button(game) {
                selectedGame = game
                buildHome()
            }
        }
        button("Back to Dashboard") { buildHome() }
    }

    private fun showMonitor() {
        shell("Device Monitor", "Live device details available through standard Android APIs.")
        card("DEVICE", deviceSummary())
        card("BATTERY", batterySummary())
        card("STORAGE", storageSummary())
        val am = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val info = android.app.ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        val totalRam = info.totalMem / (1024L * 1024L)
        val freeRam = info.availMem / (1024L * 1024L)
        card("MEMORY", "Total RAM: ${totalRam} MB\nAvailable RAM: ${freeRam} MB")
        val hz = resources.displayMetrics.refreshRate
        card("DISPLAY", "Resolution: ${resources.displayMetrics.widthPixels} × ${resources.displayMetrics.heightPixels}\nReported refresh rate: ${String.format(Locale.US, "%.0f Hz", hz)}")
        card("CPU", "Processor details vary by Android version and device. Exact live CPU usage is not exposed consistently to ordinary apps.")
        button("Refresh") { showMonitor() }
        button("Back to Dashboard") { buildHome() }
    }

    private fun showGuides() {
        shell("Gaming Guides", "General, safe settings suggestions. Options vary by device and game.")
        card("PERFORMANCE", "Close unused apps before a session. Keep enough free storage. Use the phone's built-in game mode if available.")
        card("DISPLAY", "Use a refresh-rate option supported by your phone and game. Higher settings may use more battery and create more heat.")
        card("NETWORK", "Prefer a stable connection. If possible, stay near your Wi-Fi router and pause large downloads during online play.")
        card("THERMALS", "Avoid playing while the phone is unusually hot. Let it cool naturally and keep vents or case openings unobstructed.")
        card("GAME PROFILE", selectedGame + "\nUse the in-game graphics and control options that your device can run reliably.")
        button("Back to Dashboard") { buildHome() }
    }

    private fun showTapCounter() {
        shell("Touch Tap Counter", "A simple tap practice counter. It does not measure hardware touch latency.")
        val counterText = text("Taps: 0", 24f, blue, true)
        content.addView(counterText)
        var count = 0
        button("TAP") {
            count++
            counterText.text = "Taps: $count"
        }
        button("Reset") {
            count = 0
            counterText.text = "Taps: 0"
        }
        button("Back to Dashboard") { buildHome() }
    }

    private fun showAbout() {
        shell("About POTATO GRAPHICS V3", "English edition")
        card("PURPOSE", "A lightweight gaming companion with device information, game profiles, guides, and simple practice tools.")
        card("TRANSPARENCY", "No root access required. No game-file modification, cheats, injection, FPS unlocking, or guaranteed boost claims.")
        card("PRIVACY", "This starter version does not require an account and does not send device information to a server.")
        card("LIMITATIONS", "Some readings depend on Android version and manufacturer. Battery temperature is not the same as the phone's overall temperature. Network speed and ping testing are not included in this starter build.")
        button("Back to Dashboard") { buildHome() }
    }
}
