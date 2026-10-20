package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.AppOption
import com.example.data.model.AssignedTask
import com.example.data.model.InspectionReport
import com.example.data.model.UserAccount
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object WebVersionExportUtil {

    fun buildWebHtmlWithSeedData(
        context: Context,
        reports: List<InspectionReport> = emptyList(),
        users: List<UserAccount> = emptyList(),
        tasks: List<AssignedTask> = emptyList(),
        customOptions: List<AppOption> = emptyList()
    ): String {
        val rawHtml = context.assets.open("index.html").bufferedReader(Charsets.UTF_8).use { it.readText() }

        val reportsArray = JSONArray()
        reports.forEach { r ->
            val obj = JSONObject().apply {
                put("id", r.id.ifBlank { "rep_${System.currentTimeMillis()}" })
                put("userId", r.userId)
                put("reportCategory", r.reportCategory)
                put("reportMonth", r.reportMonth)
                put("inspectionDate", r.inspectionDate)
                put("inspectorName", r.inspectorName)
                put("ownerName", r.ownerName)
                put("ownerMobile", r.ownerMobile)
                put("buildingAddress", r.buildingAddress)
                put("mouzaName", r.mouzaName)
                put("thanaName", r.thanaName)
                put("buildingType", r.buildingType)
                put("previousInspectionCount", r.previousInspectionCount)
                put("authorizedOfficerInspected", r.authorizedOfficerInspected)
                put("deviationType", r.deviationType)
                put("actionTaken", r.actionTaken)
                put("previousMonthBalance", r.previousMonthBalance)
                put("currentMonthReceived", r.currentMonthReceived)
                put("totalReceived", r.totalReceived)
                put("unresolvedAtMonthEnd", r.unresolvedAtMonthEnd)
                put("unresolvedDurationCategory", r.unresolvedDurationCategory)
                put("planSubmitted", r.planSubmitted)
                put("planApproved", r.planApproved)
                put("planRejected", r.planRejected)
                put("planCompleted", r.planCompleted)
                put("planWorkInProgress", r.planWorkInProgress)
                put("planSentForFeedback", r.planSentForFeedback)
                put("planCompletedWithin45Days", r.planCompletedWithin45Days)
                put("planCompletedMoreThan45Days", r.planCompletedMoreThan45Days)
                put("occupancyPreviousMonthBalance", r.occupancyPreviousMonthBalance)
                put("occupancyPrevMonthBalance", r.occupancyPreviousMonthBalance)
                put("occupancyCurrentMonthReceived", r.occupancyCurrentMonthReceived)
                put("occupancyTotalApplications", r.occupancyTotalApplications)
                put("occupancyTotalReceived", r.occupancyTotalApplications)
                put("occupancyInspectedBuildings", r.occupancyInspectedBuildings)
                put("occupancyIssuedCount", r.occupancyIssuedCount)
                put("occupancyNotIssuedCount", r.occupancyNotIssuedCount)
                put("occupancyNotIssuedReason", r.occupancyNotIssuedReason)
                put("mobileCourtDate", r.mobileCourtDate.ifBlank { r.inspectionDate })
                put("magistrateName", r.magistrateName)
                put("mobileCourtBuildingStage", r.buildingType.ifBlank { "নির্মাণাধীন" })
                put("mobileCourtFineAmount", r.mobileCourtFineAmount)
                put("mobileCourtMeterCut", r.mobileCourtMeterCut)
                put("mobileCourtMeterCount", r.mobileCourtMeterCount)
                put("mobileCourtElectricityDisconnectedCount", if (r.mobileCourtMeterCut) 1 else 0)
                put("mobileCourtMeterRemovedCount", r.mobileCourtMeterCount)
                put("mobileCourtUndertakingSubmitted", r.mobileCourtUndertakingSubmitted)
                put("remarks", r.remarks)
            }
            reportsArray.put(obj)
        }

        val usersArray = JSONArray()
        users.forEach { u ->
            val obj = JSONObject().apply {
                put("id", u.id.ifBlank { u.userId })
                put("userId", u.userId.ifBlank { u.id })
                put("email", u.email)
                put("inspectorName", u.inspectorName)
                put("designation", u.designation)
                put("officeName", u.officeName)
                put("phoneNumber", u.phoneNumber)
                put("approvalStatus", u.approvalStatus)
                put("role", u.role)
            }
            usersArray.put(obj)
        }

        val tasksArray = JSONArray()
        tasks.forEach { t ->
            val obj = JSONObject().apply {
                put("id", t.id.ifBlank { "task_${System.currentTimeMillis()}" })
                put("title", t.title)
                put("description", t.description)
                put("assignedToUserId", t.assignedToUserId)
                put("assignedToName", t.assignedToName)
                put("assignedToDesignation", t.assignedToDesignation)
                put("assignedToPhone", t.assignedToPhone)
                put("createdByUserId", t.createdByUserId)
                put("createdByName", t.createdByName)
                put("dueDate", t.dueDate)
                put("priority", t.priority)
                put("status", t.status)
                put("completionNote", t.completionNote)
                put("buildingAddress", t.buildingAddress)
            }
            tasksArray.put(obj)
        }

        val optionsArray = JSONArray()
        customOptions.forEach { o ->
            val obj = JSONObject().apply {
                put("id", o.id.ifBlank { "opt_${System.currentTimeMillis()}" })
                put("group", o.group)
                put("name", o.name)
                put("subGroup", o.subGroup)
                put("sortOrder", o.sortOrder)
            }
            optionsArray.put(obj)
        }

        val seedScript = """
            <script>
            (function() {
              try {
                var exRep = localStorage.getItem('rajuk_web_inspection_reports_v1');
                if ((!exRep || exRep === '[]') && ${reportsArray.length()} > 0) {
                  localStorage.setItem('rajuk_web_inspection_reports_v1', ${JSONObject.quote(reportsArray.toString())});
                }
                var exUsr = localStorage.getItem('rajuk_web_user_accounts_v1');
                if ((!exUsr || exUsr === '[]') && ${usersArray.length()} > 0) {
                  localStorage.setItem('rajuk_web_user_accounts_v1', ${JSONObject.quote(usersArray.toString())});
                }
                var exTsk = localStorage.getItem('rajuk_web_assigned_tasks_v1');
                if ((!exTsk || exTsk === '[]') && ${tasksArray.length()} > 0) {
                  localStorage.setItem('rajuk_web_assigned_tasks_v1', ${JSONObject.quote(tasksArray.toString())});
                }
                var exOpt = localStorage.getItem('rajuk_web_custom_options_v1');
                if ((!exOpt || exOpt === '[]') && ${optionsArray.length()} > 0) {
                  localStorage.setItem('rajuk_web_custom_options_v1', ${JSONObject.quote(optionsArray.toString())});
                }
              } catch (e) {}
            })();
            </script>
        """.trimIndent()

        return rawHtml.replace("</head>", "$seedScript\n</head>")
    }

    fun downloadWebVersionToDevice(
        context: Context,
        reports: List<InspectionReport> = emptyList(),
        users: List<UserAccount> = emptyList(),
        tasks: List<AssignedTask> = emptyList(),
        customOptions: List<AppOption> = emptyList()
    ) {
        try {
            val htmlContent = buildWebHtmlWithSeedData(context, reports, users, tasks, customOptions)
            val bytes = htmlContent.toByteArray(Charsets.UTF_8)
            val fileName = "RAJUK_Office_Report_Web_App.html"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "text/html")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        out.write(bytes)
                        out.flush()
                    }
                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
            } else {
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, fileName)
                FileOutputStream(file).use { out ->
                    out.write(bytes)
                    out.flush()
                }
            }

            Toast.makeText(
                context,
                "ওয়েব ভার্সন ফাইলটি Downloads ফোল্ডারে '$fileName' নামে সেভ হয়েছে! যেকোনো ব্রাউজারে ওপেন করতে পারবেন।",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "ওয়েব ভার্সন ডাউনলোডে সমস্যা: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun shareWebVersionFile(
        context: Context,
        reports: List<InspectionReport> = emptyList(),
        users: List<UserAccount> = emptyList(),
        tasks: List<AssignedTask> = emptyList(),
        customOptions: List<AppOption> = emptyList()
    ) {
        try {
            val htmlContent = buildWebHtmlWithSeedData(context, reports, users, tasks, customOptions)
            val webDir = File(context.cacheDir, "web")
            if (!webDir.exists()) webDir.mkdirs()
            val fileName = "RAJUK_Office_Report_Web_App.html"
            val htmlFile = File(webDir, fileName)
            FileOutputStream(htmlFile).use { out ->
                out.write(htmlContent.toByteArray(Charsets.UTF_8))
                out.flush()
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                htmlFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/html"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "রাজউক অফিস মাসিক প্রতিবেদন - ওয়েব অ্যাপ ($fileName)। ফাইলটি ডাউনলোড করে যেকোনো কম্পিউটার বা মোবাইলের ব্রাউজারে (Chrome/Edge/Safari/Firefox) ওপেন করুন।"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "ওয়েব ভার্সন শেয়ার বা ওপেন করুন").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "ওয়েব ভার্সন শেয়ারে সমস্যা: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun openWebVersionInBrowser(
        context: Context,
        reports: List<InspectionReport> = emptyList(),
        users: List<UserAccount> = emptyList(),
        tasks: List<AssignedTask> = emptyList(),
        customOptions: List<AppOption> = emptyList()
    ) {
        try {
            downloadWebVersionToDevice(context, reports, users, tasks, customOptions)
            val htmlContent = buildWebHtmlWithSeedData(context, reports, users, tasks, customOptions)
            val webDir = File(context.cacheDir, "web")
            if (!webDir.exists()) webDir.mkdirs()
            val fileName = "RAJUK_Office_Report_Web_App.html"
            val htmlFile = File(webDir, fileName)
            FileOutputStream(htmlFile).use { out ->
                out.write(htmlContent.toByteArray(Charsets.UTF_8))
                out.flush()
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                htmlFile
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/html")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(viewIntent, "যেকোনো ব্রাউজারে ওপেন করুন").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            shareWebVersionFile(context, reports, users, tasks, customOptions)
        }
    }
}
