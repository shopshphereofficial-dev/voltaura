package com.voltaura.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.voltaura.app.battery.BatteryReader
import com.voltaura.app.data.Prefs
import com.voltaura.app.overlay.ChargingAnimationView
import com.voltaura.app.overlay.EdgeGlowView
import com.voltaura.app.service.ChargingOverlayService
import com.voltaura.app.ui.theme.Bg
import com.voltaura.app.ui.theme.Cyan
import com.voltaura.app.ui.theme.Surface1
import com.voltaura.app.ui.theme.Surface2
import com.voltaura.app.ui.theme.TextDim
import com.voltaura.app.ui.theme.TextMain
import com.voltaura.app.ui.theme.Violet
import kotlinx.coroutines.delay
import kotlin.math.abs

enum class Screen { HOME, CHARGING, GLOW }

@Composable
fun VoltAuraApp() {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }

    var screen by remember { mutableStateOf(Screen.HOME) }

    var chargingEnabled by remember { mutableStateOf(prefs.chargingEnabled) }
    var chargingStyle by remember { mutableStateOf(prefs.chargingStyle) }
    var glowEnabled by remember { mutableStateOf(prefs.glowEnabled) }
    var glowStyle by remember { mutableStateOf(prefs.glowStyle) }
    var glowColor by remember { mutableStateOf(prefs.glowColor) }
    var glowWidth by remember { mutableStateOf(prefs.glowWidth) }
    var glowDuration by remember { mutableStateOf(prefs.glowDuration) }

    var overlayGranted by remember { mutableStateOf(PermissionUtils.canDrawOverlays(context)) }
    var notifAccess by remember { mutableStateOf(PermissionUtils.isNotificationAccessGranted(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                overlayGranted = PermissionUtils.canDrawOverlays(context)
                notifAccess = PermissionUtils.isNotificationAccessGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    Surface(color = Bg, modifier = Modifier.fillMaxSize()) {
        when (screen) {
            Screen.HOME -> HomeScreen(
                overlayGranted = overlayGranted,
                notifAccess = notifAccess,
                onOpenCharging = { screen = Screen.CHARGING },
                onOpenGlow = { screen = Screen.GLOW },
                onGrantOverlay = { PermissionUtils.openOverlaySettings(context) },
                onGrantNotif = { PermissionUtils.openNotificationListenerSettings(context) },
            )

            Screen.CHARGING -> ChargingScreen(
                enabled = chargingEnabled,
                onEnabled = {
                    chargingEnabled = it
                    prefs.chargingEnabled = it
                    if (it) ChargingOverlayService.start(context) else ChargingOverlayService.stop(context)
                },
                style = chargingStyle,
                onStyle = { chargingStyle = it; prefs.chargingStyle = it },
                overlayGranted = overlayGranted,
                onGrantOverlay = { PermissionUtils.openOverlaySettings(context) },
                onBack = { screen = Screen.HOME },
            )

            Screen.GLOW -> GlowScreen(
                enabled = glowEnabled,
                onEnabled = { glowEnabled = it; prefs.glowEnabled = it },
                style = glowStyle,
                onStyle = { glowStyle = it; prefs.glowStyle = it },
                color = glowColor,
                onColor = { glowColor = it; prefs.glowColor = it },
                width = glowWidth,
                onWidth = { glowWidth = it; prefs.glowWidth = it },
                duration = glowDuration,
                onDuration = { glowDuration = it; prefs.glowDuration = it },
                notifAccess = notifAccess,
                onGrantNotif = { PermissionUtils.openNotificationListenerSettings(context) },
                onBack = { screen = Screen.HOME },
            )
        }
    }
}

@Composable
fun HomeScreen(
    overlayGranted: Boolean,
    notifAccess: Boolean,
    onOpenCharging: () -> Unit,
    onOpenGlow: () -> Unit,
    onGrantOverlay: () -> Unit,
    onGrantNotif: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Text("VoltAura", style = MaterialTheme.typography.displaySmall, color = Cyan, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("Premium charging animations & notification glow", color = TextDim)
        Spacer(Modifier.height(28.dp))

        FeatureCard(
            title = "Charging Animation",
            subtitle = "Animated effects when you plug in, with live watts, amps & speed",
            accent = Cyan,
            onClick = onOpenCharging,
        )
        Spacer(Modifier.height(16.dp))
        FeatureCard(
            title = "Notification Glow",
            subtitle = "Colourful edge lighting when a notification arrives",
            accent = Violet,
            onClick = onOpenGlow,
        )

        Spacer(Modifier.height(28.dp))
        Text("Permissions", style = MaterialTheme.typography.titleMedium, color = TextMain)
        Spacer(Modifier.height(10.dp))
        PermissionRow("Draw over other apps", overlayGranted, onGrantOverlay)
        Spacer(Modifier.height(10.dp))
        PermissionRow("Notification access", notifAccess, onGrantNotif)
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun FeatureCard(title: String, subtitle: String, accent: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.horizontalGradient(listOf(accent.copy(alpha = 0.22f), Surface2)))
            .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge, color = TextMain, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = TextDim)
            Spacer(Modifier.height(14.dp))
            Text("Open", color = accent, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun PermissionRow(label: String, granted: Boolean, onGrant: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface1)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = TextMain)
        if (granted) {
            Text("Granted", color = Cyan, fontWeight = FontWeight.Medium)
        } else {
            Button(onClick = onGrant, colors = ButtonDefaults.buttonColors(containerColor = Violet)) {
                Text("Allow")
            }
        }
    }
}

@Composable
fun TopBar(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onBack) { Text("Back", color = Cyan) }
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = TextMain, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun StatBox(modifier: Modifier, label: String, value: String, unit: String) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Surface1)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, color = TextDim, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            if (unit.isEmpty()) value else "$value $unit",
            color = TextMain,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun StyleGrid(
    count: Int,
    selected: Int,
    labels: List<String>,
    onSelect: (Int) -> Unit,
    preview: @Composable (Int, Modifier) -> Unit,
) {
    Column {
        val rows = (count + 1) / 2
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (col in 0 until 2) {
                    val i = row * 2 + col
                    if (i < count) {
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(
                                        width = if (selected == i) 2.dp else 1.dp,
                                        color = if (selected == i) Cyan else Surface2,
                                        shape = RoundedCornerShape(16.dp),
                                    )
                                    .clickable { onSelect(i) }
                            ) {
                                preview(i, Modifier.fillMaxSize())
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                labels.getOrElse(i) { "Style $i" },
                                color = if (selected == i) Cyan else TextDim,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
fun ChargingScreen(
    enabled: Boolean,
    onEnabled: (Boolean) -> Unit,
    style: Int,
    onStyle: (Int) -> Unit,
    overlayGranted: Boolean,
    onGrantOverlay: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var reading by remember { mutableStateOf(BatteryReader.read(context)) }
    LaunchedEffect(Unit) {
        while (true) {
            reading = BatteryReader.read(context)
            delay(1500)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TopBar("Charging Animation", onBack)
        Spacer(Modifier.height(18.dp))

        if (!overlayGranted) {
            Button(
                onClick = onGrantOverlay,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Violet),
            ) { Text("Grant 'Draw over other apps' permission") }
            Spacer(Modifier.height(16.dp))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatBox(Modifier.weight(1f), "Watts", String.format("%.1f", reading.watts), "W")
            StatBox(Modifier.weight(1f), "Current", String.format("%.2f", abs(reading.currentUa) / 1_000_000.0), "A")
            StatBox(Modifier.weight(1f), "Volts", String.format("%.1f", reading.voltageMv / 1000.0), "V")
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatBox(Modifier.weight(1f), "Battery", if (reading.levelPercent in 0..100) "${reading.levelPercent}%" else "--", "")
            StatBox(Modifier.weight(1f), "Source", reading.source, "")
            StatBox(Modifier.weight(1f), "Temp", String.format("%.1f", reading.temperatureC), "C")
        }

        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Enable charging animation", color = TextMain, modifier = Modifier.weight(1f))
            Switch(checked = enabled, onCheckedChange = onEnabled)
        }
        Spacer(Modifier.height(20.dp))
        Text("Choose a style", style = MaterialTheme.typography.titleMedium, color = TextMain)
        Spacer(Modifier.height(12.dp))

        StyleGrid(
            count = 6,
            selected = style,
            labels = listOf("Neon Ring", "Liquid Fill", "Particles", "Energy Bolts", "Pulse", "Wave Bars"),
            onSelect = onStyle,
            preview = { i, mod ->
                AndroidView(
                    factory = { ctx ->
                        ChargingAnimationView(ctx).apply {
                            this.style = i
                            batteryPercent = 72
                        }
                    },
                    modifier = mod,
                )
            },
        )
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun GlowScreen(
    enabled: Boolean,
    onEnabled: (Boolean) -> Unit,
    style: Int,
    onStyle: (Int) -> Unit,
    color: Int,
    onColor: (Int) -> Unit,
    width: Int,
    onWidth: (Int) -> Unit,
    duration: Int,
    onDuration: (Int) -> Unit,
    notifAccess: Boolean,
    onGrantNotif: () -> Unit,
    onBack: () -> Unit,
) {
    val swatches = listOf(
        0xFF00E5FF.toInt(), 0xFF7C4DFF.toInt(), 0xFFFF4DA6.toInt(),
        0xFFFFD54A.toInt(), 0xFF00E676.toInt(), 0xFFFF5252.toInt(),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TopBar("Notification Glow", onBack)
        Spacer(Modifier.height(18.dp))

        if (!notifAccess) {
            Button(
                onClick = onGrantNotif,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Violet),
            ) { Text("Grant Notification access") }
            Spacer(Modifier.height(16.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Enable notification glow", color = TextMain, modifier = Modifier.weight(1f))
            Switch(checked = enabled, onCheckedChange = onEnabled)
        }

        Spacer(Modifier.height(20.dp))
        Text("Style", style = MaterialTheme.typography.titleMedium, color = TextMain)
        Spacer(Modifier.height(12.dp))
        StyleGrid(
            count = 5,
            selected = style,
            labels = listOf("Solid", "Rainbow", "Gradient", "Pulse", "Aurora"),
            onSelect = onStyle,
            preview = { i, mod ->
                AndroidView(
                    factory = { ctx ->
                        EdgeGlowView(ctx).apply {
                            this.style = i
                            baseColor = color
                            strokeWidthPx = 30f
                        }
                    },
                    modifier = mod,
                )
            },
        )

        Spacer(Modifier.height(20.dp))
        Text("Colour", style = MaterialTheme.typography.titleMedium, color = TextMain)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            swatches.forEach { c ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(c))
                        .border(if (color == c) 3.dp else 0.dp, Color.White, RoundedCornerShape(12.dp))
                        .clickable { onColor(c) }
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Thickness: $width px", color = TextMain)
        Slider(value = width.toFloat(), onValueChange = { onWidth(it.toInt()) }, valueRange = 8f..60f)

        Spacer(Modifier.height(8.dp))
        Text("Duration: $duration ms", color = TextMain)
        Slider(value = duration.toFloat(), onValueChange = { onDuration(it.toInt()) }, valueRange = 800f..5000f)

        Spacer(Modifier.height(30.dp))
    }
}
