@file:OptIn(ExperimentalTvMaterial3Api::class)
package com.tvos.launcher

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.tv.material3.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class AppItem(val label: String, val pkg: String, val intent: Intent, val icon: ImageBitmap?)

// Favourites pehle dikhenge. Apne apps ke package yahan add karo.
val PRIORITY = listOf("com.spotify.tv.android", "in.startv.hotstar", "com.crunchyroll.crunchyroid",
    "com.google.android.youtube.tv", "com.netflix.ninja")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Home(loadApps()) }
    }

    private fun loadApps(): List<AppItem> {
        val pm = packageManager
        fun q(cat: String) = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(cat), 0)
        val apps = (q(Intent.CATEGORY_LEANBACK_LAUNCHER) + q(Intent.CATEGORY_LAUNCHER))
            .distinctBy { it.activityInfo.packageName }
            .filter { it.activityInfo.packageName != packageName }
            .map {
                val i = Intent(Intent.ACTION_MAIN).setClassName(it.activityInfo.packageName, it.activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                AppItem(it.loadLabel(pm).toString(), it.activityInfo.packageName, i,
                    runCatching { it.loadIcon(pm).toBitmap(256, 256).asImageBitmap() }.getOrNull())
            }
            .sortedWith(compareBy({ PRIORITY.indexOf(it.pkg).let { i -> if (i < 0) 99 else i } }, { it.label.lowercase() }))
        val settings = AppItem("Settings", "settings", Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), null)
        return apps + settings
    }
}

@Composable
fun Home(apps: List<AppItem>) {
    val ctx = LocalContext.current
    var focused by remember { mutableStateOf(apps.firstOrNull()) }
    val now by produceState(Date()) { while (true) { value = Date(); delay(10_000) } }
    val cal = Calendar.getInstance().apply { time = now }
    val h = cal.get(Calendar.HOUR_OF_DAY)
    // Dynamic wallpaper: din ke time ke hisaab se gradient badalta hai
    val (top, bottom, greet) = when (h) {
        in 5..11 -> Triple(Color(0xFF3A6EA5), Color(0xFFF2A65A), "Good morning")
        in 12..16 -> Triple(Color(0xFF1F5FA8), Color(0xFF6DB3E8), "Good afternoon")
        in 17..20 -> Triple(Color(0xFF3B2A5E), Color(0xFFE0675A), "Good evening")
        else -> Triple(Color(0xFF05070F), Color(0xFF1B2440), "Good night")
    }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, bottom)))) {
        Column(Modifier.fillMaxSize().padding(horizontal = 56.dp, vertical = 40.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(greet, fontSize = 22.sp, color = Color.White.copy(0.8f))
                Text(SimpleDateFormat("EEE, d MMM  hh:mm a", Locale.getDefault()).format(now), fontSize = 22.sp, color = Color.White)
            }
            Spacer(Modifier.weight(1f))
            Text(focused?.label ?: "", fontSize = 60.sp, color = Color.White)
            Text("OK dabao kholne ke liye", fontSize = 18.sp, color = Color.White.copy(0.7f))
            Spacer(Modifier.height(28.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(22.dp), contentPadding = PaddingValues(vertical = 24.dp, horizontal = 8.dp)) {
                items(apps) { app ->
                    Surface(
                        onClick = { runCatching { ctx.startActivity(app.intent) } },
                        modifier = Modifier.size(170.dp).onFocusChanged { if (it.isFocused) focused = app },
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(22.dp)),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.15f),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color.White.copy(0.14f), focusedContainerColor = Color.White.copy(0.32f),
                            contentColor = Color.White, focusedContentColor = Color.White)
                    ) {
                        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            if (app.icon != null) Image(app.icon, app.label, Modifier.size(84.dp).clip(RoundedCornerShape(16.dp)))
                            else Text("⚙", fontSize = 56.sp)
                            Spacer(Modifier.height(10.dp))
                            Text(app.label, fontSize = 15.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
