package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.AppOption
import com.example.data.model.AssignedTask
import com.example.data.model.InspectionReport
import com.example.data.model.UserAccount
import com.example.util.WebVersionExportUtil

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebAppInteractiveScreen(
    reports: List<InspectionReport>,
    users: List<UserAccount>,
    tasks: List<AssignedTask>,
    customOptions: List<AppOption>,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val context = LocalContext.current
    val seededHtml = remember(reports, users, tasks, customOptions) {
        WebVersionExportUtil.buildWebHtmlWithSeedData(
            context = context,
            reports = reports,
            users = users,
            tasks = tasks,
            customOptions = customOptions
        )
    }

    var webViewRef: WebView? = remember { null }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF073440))
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Top Action Control Bar for Web Version
        Surface(
            color = Color(0xFF073440),
            contentColor = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_interactive_web_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "অ্যাপে ফিরে যান",
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = "🌐 ওয়েব ভার্সন (সকল ফিচার সহ)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "RAJUK_Office_Report_Web_App.html",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFDE68A)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            webViewRef?.let { wv ->
                                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                                val adapter = wv.createPrintDocumentAdapter("RAJUK_Web_Report")
                                printManager?.print(
                                    "RAJUK_Web_Report",
                                    adapter,
                                    PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build()
                                )
                            }
                        },
                        modifier = Modifier.testTag("web_print_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Print,
                            contentDescription = "প্রিন্ট / PDF সেভ করুন",
                            tint = Color.White
                        )
                    }

                    Button(
                        onClick = {
                            WebVersionExportUtil.downloadWebVersionToDevice(
                                context = context,
                                reports = reports,
                                users = users,
                                tasks = tasks,
                                customOptions = customOptions
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF59E0B),
                            contentColor = Color(0xFF073440)
                        ),
                        modifier = Modifier.testTag("web_download_html_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = "HTML ডাউনলোড"
                        )
                        Text(
                            text = " .html",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = {
                            WebVersionExportUtil.shareWebVersionFile(
                                context = context,
                                reports = reports,
                                users = users,
                                tasks = tasks,
                                customOptions = customOptions
                            )
                        },
                        modifier = Modifier.testTag("web_share_html_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "শেয়ার করুন",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            WebVersionExportUtil.openWebVersionInBrowser(
                                context = context,
                                reports = reports,
                                users = users,
                                tasks = tasks,
                                customOptions = customOptions
                            )
                        },
                        modifier = Modifier.testTag("web_external_browser_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OpenInBrowser,
                            contentDescription = "ব্রাউজারে খুলুন",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Full-featured Embedded Web Application View
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewRef = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()
                    loadDataWithBaseURL(
                        "https://rajuk.web.local/",
                        seededHtml,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            }
        )
    }
}
