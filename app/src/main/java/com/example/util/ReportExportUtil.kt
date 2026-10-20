package com.example.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import com.example.data.model.InspectionPresets
import com.example.data.model.InspectionReport
import com.example.data.model.InspectorProfile
import com.example.data.model.MonthlySummaryStats
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream

enum class ReportColumn(
    val id: String,
    val titleBangla: String,
    val defaultSelected: Boolean = true
) {
    SL("sl", "১. ক্রমিক নং", true),
    CATEGORY("category", "২. ক্যাটাগরি", true),
    INSPECTION_DATE("date", "৩. পরিদর্শনের তারিখ", true),
    OWNER_NAME("owner", "৪. ভবন মালিকের নাম", true),
    BUILDING_ADDRESS("address", "৫. ভবনের ঠিকানা", true),
    MOUZA_NAME("mouza", "৬. মৌজার নাম", false),
    THANA_NAME("thana", "৭. থানার নাম", false),
    OWNER_MOBILE("mobile", "৮. মালিকের মোবাইল", true),
    BUILDING_TYPE("bld_type", "৯. ভবনের ধরণ", true),
    PREV_INSPECTION_COUNT("prev_insp", "১০. আগে কতবার পরিদর্শন", false),
    AUTH_OFFICER_INSPECTED("auth_insp", "১১. অথরাইজড অফিসার পরিদর্শন?", false),
    DEVIATION_TYPE("deviation", "১২. ব্যত্যয়কৃত ভবনের ধরণ", true),
    ACTION_TAKEN("action", "১৩. গৃহীত ব্যবস্থা", true),
    DESIGN_APPROVAL("design", "১৪. নকশা অনুমোদনের তথ্য", false),
    OCCUPANCY_CERT("occupancy", "১৫. অকুপেন্সী সনদের তথ্য", false),
    MOBILE_COURT("mobile_court", "১৬. মোবাইল কোর্ট (জরিমানা/মিটার)", false),
    UNRESOLVED_DETAILS("unresolved", "১৭. অনিষ্পন্ন তথ্য", false),
    INSPECTOR_NAME("inspector", "১৮. পরিদর্শকের নাম", false),
    REMARKS("remarks", "১৯. মন্তব্য / নোট", true)
}

object ReportExportUtil {

    fun buildFormattedTextReport(
        reportMonth: String,
        reports: List<InspectionReport>,
        profile: InspectorProfile?,
        stats: MonthlySummaryStats
    ): String {
        val monthBangla = InspectionPresets.formatReportMonthBangla(reportMonth)
        val inspector = profile?.inspectorName?.ifBlank { null }
            ?: reports.firstOrNull()?.inspectorName
            ?: "নির্ধারিত নয়"
        val designation = profile?.designation?.takeIf { it.isNotBlank() } ?: ""
        val office = profile?.officeName?.takeIf { it.isNotBlank() } ?: ""

        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("   ইমারত পরিদর্শনের মাসিক প্রতিবেদন")
        if (office.isNotBlank()) sb.appendLine("   $office")
        sb.appendLine("==========================================")
        sb.appendLine("প্রতিবেদনের মাস: $monthBangla")
        sb.appendLine("ইমারত পরিদর্শকের নাম: $inspector")
        if (designation.isNotBlank()) sb.appendLine("পদবী / জোন: $designation")
        sb.appendLine("------------------------------------------")
        sb.appendLine("মাসিক সারসংক্ষেপ:")
        sb.appendLine("• মোট পরিদর্শনকৃত ভবন: ${InspectionPresets.toBanglaDigits(stats.totalInspections)} টি")
        sb.appendLine("• নির্মিত ভবন: ${InspectionPresets.toBanglaDigits(stats.constructedBuildingCount)} টি")
        sb.appendLine("• নির্মাণাধীন ভবন: ${InspectionPresets.toBanglaDigits(stats.underConstructionBuildingCount)} টি")
        sb.appendLine("• অথরাইজড অফিসার পরিদর্শন করেছেন: ${InspectionPresets.toBanglaDigits(stats.authorizedOfficerVisitedCount)} টি")
        sb.appendLine("• ব্যত্যয়কৃত ভবন সংখ্যা: ${InspectionPresets.toBanglaDigits(stats.deviatedBuildingCount)} টি")
        sb.appendLine("• পুনঃপরিদর্শনকৃত ভবন: ${InspectionPresets.toBanglaDigits(stats.repeatInspectionCount)} টি")
        sb.appendLine("==========================================")
        sb.appendLine()

        if (reports.isEmpty()) {
            sb.appendLine("এই মাসে কোনো পরিদর্শন তথ্য লিপিবদ্ধ করা হয়নি।")
        } else {
            reports.forEachIndexed { index, item ->
                val sl = InspectionPresets.toBanglaDigits(index + 1)
                val cat = item.reportCategory.ifBlank { "ভবন পরিদর্শন সম্পর্কিত রিপোর্ট" }
                sb.appendLine("ক্রমিক নং: $sl [$cat]")
                if (InspectionPresets.isDesignApprovalCategory(cat)) {
                    sb.appendLine("প্রতিবেদনের মাস: ${InspectionPresets.formatReportMonthBangla(item.reportMonth)}")
                    sb.appendLine("ইমারত পরিদর্শকের নাম: ${item.inspectorName}")
                    sb.appendLine("১. Submitted (দাখিলকৃত): ${InspectionPresets.toBanglaDigits(item.planSubmitted)} টি")
                    sb.appendLine("২. Approved (অনুমোদিত): ${InspectionPresets.toBanglaDigits(item.planApproved)} টি")
                    sb.appendLine("৩. Rejected (বাতিল): ${InspectionPresets.toBanglaDigits(item.planRejected)} টি")
                    sb.appendLine("৪. Completed (Approved + Rejected + Sent for Applicants Feedback): ${InspectionPresets.toBanglaDigits(item.planCompleted)} টি")
                    sb.appendLine("৫. Work In Progress (চলমান): ${InspectionPresets.toBanglaDigits(item.planWorkInProgress)} টি")
                    sb.appendLine("৬. Sent For Applicants Feedback (মতামতের জন্য প্রেরিত): ${InspectionPresets.toBanglaDigits(item.planSentForFeedback)} টি")
                    sb.appendLine("৭. Completed Within 45 Working Days (≤৪৫ দিন): ${InspectionPresets.toBanglaDigits(item.planCompletedWithin45Days)} টি")
                    sb.appendLine("৮. Completed In More Than 45 Working Days (>৪৫ দিন): ${InspectionPresets.toBanglaDigits(item.planCompletedMoreThan45Days)} টি")
                    if (item.remarks.isNotBlank()) {
                        sb.appendLine("মন্তব্য: ${item.remarks}")
                    }
                } else if (InspectionPresets.isOccupancyCertificateCategory(cat)) {
                    val prevBal = if (item.occupancyPreviousMonthBalance > 0L) item.occupancyPreviousMonthBalance else item.previousMonthBalance
                    val currRec = if (item.occupancyCurrentMonthReceived > 0L) item.occupancyCurrentMonthReceived else item.currentMonthReceived
                    val totApp = if (item.occupancyTotalApplications > 0L) item.occupancyTotalApplications else item.totalReceived
                    val inspBld = if (item.occupancyInspectedBuildings > 0L) item.occupancyInspectedBuildings else item.previousInspectionCount
                    sb.appendLine("প্রতিবেদনের মাস: ${InspectionPresets.formatReportMonthBangla(item.reportMonth)}")
                    sb.appendLine("ইমারত পরিদর্শকের নাম: ${item.inspectorName}")
                    sb.appendLine("১. পূর্ববর্তী মাসের জের: ${InspectionPresets.toBanglaDigits(prevBal)} টি")
                    sb.appendLine("২. বর্তমান মাসে প্রাপ্ত আবেদনের সংখ্যা: ${InspectionPresets.toBanglaDigits(currRec)} টি")
                    sb.appendLine("৩. জেরসহ মোট আবেদন সংখ্যা: ${InspectionPresets.toBanglaDigits(totApp)} টি")
                    sb.appendLine("৪. পরিদর্শনকৃত ভবনের সংখ্যা: ${InspectionPresets.toBanglaDigits(inspBld)} টি")
                    sb.appendLine("৫. কতটি Occupancy Certificate প্রদান করা হয়েছে: ${InspectionPresets.toBanglaDigits(item.occupancyIssuedCount)} টি")
                    sb.appendLine("৬. কতটি Occupancy Certificate প্রদান করা হয় নি: ${InspectionPresets.toBanglaDigits(item.occupancyNotIssuedCount)} টি")
                    if (item.occupancyNotIssuedReason.isNotBlank()) {
                        sb.appendLine("৭. Occupancy Certificate প্রদান না করার কারণ: ${item.occupancyNotIssuedReason}")
                    }
                    if (item.remarks.isNotBlank()) {
                        sb.appendLine("৮. মন্তব্য: ${item.remarks}")
                    }
                } else if (InspectionPresets.isMobileCourtCategory(cat)) {
                    val mcDate = item.mobileCourtDate.ifBlank { item.inspectionDate }
                    sb.appendLine("১. মোবাইল কোর্টের তারিখ: ${InspectionPresets.toBanglaDigits(mcDate)}")
                    sb.appendLine("২. ম্যাজিস্ট্রেট এর নাম: ${item.magistrateName}")
                    sb.appendLine("৩. ভবন মালিকের নাম: ${item.ownerName}")
                    sb.appendLine("৪. ভবন মালিকের মোবাইল নাম্বার: ${item.ownerMobile}")
                    sb.appendLine("৫. ভবনের ঠিকানা: ${item.buildingAddress}")
                    sb.appendLine("৬. ভবন কি নির্মিত নাকি নির্মাণাধীন: ${item.buildingType}")
                    sb.appendLine("৭. ভবনে কোন ধরনের ব্যত্যয় আছে: ${item.deviationType}")
                    sb.appendLine("৮. ভবন মালিক কে কত টাকা জরিমানা করা হয়েছে: ৳ ${InspectionPresets.toBanglaDigits(item.mobileCourtFineAmount)} টাকা")
                    val meterStatus = if (item.mobileCourtMeterCut) "হ্যাঁ (${InspectionPresets.toBanglaDigits(item.mobileCourtMeterCount)}টি)" else "না"
                    sb.appendLine("৯ ও ১০. মিটার কাটা হয়েছে কিনা: $meterStatus")
                    val undertakingStatus = if (item.mobileCourtUndertakingSubmitted) "হ্যাঁ (দাখিলকৃত)" else "না (দাখিল করেনি)"
                    sb.appendLine("১১. ভবন মালিক অঙ্গীকার নামা দাখিল করেছে কিনা: $undertakingStatus")
                    if (item.remarks.isNotBlank()) {
                        sb.appendLine("১২. মন্তব্য: ${item.remarks}")
                    }
                } else if (InspectionPresets.isUnresolvedCategory(cat)) {
                    sb.appendLine("প্রতিবেদনের মাস: ${InspectionPresets.formatReportMonthBangla(item.reportMonth)}")
                    sb.appendLine("ইমারত পরিদর্শকের নাম: ${item.inspectorName}")
                    val dur = item.unresolvedDurationCategory.ifBlank {
                        InspectionPresets.unresolvedDurationOptions.first()
                    }
                    sb.appendLine("১. গত মাসের জের: ${InspectionPresets.toBanglaDigits(item.previousMonthBalance)}")
                    sb.appendLine("২. আলোচ্য মাসের প্রাপ্তি: ${InspectionPresets.toBanglaDigits(item.currentMonthReceived)}")
                    sb.appendLine("৩. মোট প্রাপ্তি: ${InspectionPresets.toBanglaDigits(item.totalReceived)}")
                    sb.appendLine("৪. মাসের শেষ কার্যদিবসে অনিষ্পন্ন: $dur")
                    sb.appendLine("৫. মন্তব্য: ${item.remarks.ifBlank { "-" }}")
                } else {
                    sb.appendLine("১. ভবন পরিদর্শনের তারিখ: ${InspectionPresets.toBanglaDigits(item.inspectionDate)}")
                    sb.appendLine("ইমারত পরিদর্শকের নাম: ${item.inspectorName}")
                    val authStatus = if (item.authorizedOfficerInspected) "হ্যাঁ" else "না"
                    val prevCount = InspectionPresets.toBanglaDigits(item.previousInspectionCount)
                    val resolvedType = item.buildingType.ifBlank { InspectionPresets.BUILDING_TYPE_CONSTRUCTED }
                    sb.appendLine("২. ভবন মালিকের নাম: ${item.ownerName}")
                    sb.appendLine("৩. ভবনের ঠিকানা: ${item.buildingAddress}")
                    sb.appendLine("মৌজার নাম: ${item.mouzaName.ifBlank { "-" }}")
                    sb.appendLine("থানার নাম: ${item.thanaName.ifBlank { "-" }}")
                    sb.appendLine("৪. মালিকের মোবাইল নাম্বার: ${item.ownerMobile}")
                    sb.appendLine("৫. ভবনের ধরন: $resolvedType")
                    sb.appendLine("৬. আগে কতবার পরিদর্শন করা হয়েছে ($resolvedType): $prevCount বার")
                    sb.appendLine("৭. অথরাইজড অফিসার পরিদর্শন করেছে ? ($resolvedType): $authStatus")
                    sb.appendLine("৮. ব্যত্যয়কৃত ভবনের ধরণ ($resolvedType): ${item.deviationType}")
                    sb.appendLine("৯. গৃহীত ব্যবস্থা ($resolvedType): ${item.actionTaken}")
                    if (item.remarks.isNotBlank()) {
                        sb.appendLine("মন্তব্য: ${item.remarks}")
                    }
                }
                sb.appendLine("------------------------------------------")
            }
        }
        return sb.toString()
    }

    fun buildCsvContent(reports: List<InspectionReport>): String {
        val sb = StringBuilder()
        // UTF-8 BOM so Microsoft Excel renders Bangla characters properly
        sb.append('\uFEFF')
        sb.appendLine(
            "ক্রমিক নং,ক্যাটাগরি,পরিদর্শনের তারিখ,ভবন মালিকের নাম / বিবরণ,ভবনের ঠিকানা / সারাংশ,মৌজার নাম,থানার নাম,মালিকের মোবাইল নাম্বার,ভবনের ধরণ,আগে কতবার পরিদর্শন করা হয়েছে,অথরাইজড অফিসার পরিদর্শন করেছে ?,ব্যত্যয়কৃত ভবনের ধরণ,গৃহীত ব্যবস্থা,গত মাসের জের,আলোচ্য মাসের প্রাপ্তি,মোট প্রাপ্তি,মাসের শেষ কার্যদিবসে অনিষ্পন্ন,Submitted,Approved,Rejected,Completed,WIP,Sent For Feedback,Completed <= 45 Days,Completed > 45 Days,অকুপেন্সী পূর্ববর্তী জের,অকুপেন্সী বর্তমান প্রাপ্তি,অকুপেন্সী মোট আবেদন,অকুপেন্সী পরিদর্শনকৃত ভবন,অকুপেন্সী সনদ প্রদান,অকুপেন্সী সনদ অপ্রদান,অপ্রদানের কারণ,ম্যাজিস্ট্রেটের নাম,মোবাইল কোর্টের তারিখ,জরিমানা (টাকা),মিটার কর্তন,কর্তনকৃত মিটার সংখ্যা,অঙ্গীকারনামা দাখিল,মন্তব্য"
        )
        reports.forEachIndexed { idx, item ->
            val authVisited = if (item.authorizedOfficerInspected) "হ্যাঁ" else "না"
            val prevBal = if (item.occupancyPreviousMonthBalance > 0L) item.occupancyPreviousMonthBalance else item.previousMonthBalance
            val currRec = if (item.occupancyCurrentMonthReceived > 0L) item.occupancyCurrentMonthReceived else item.currentMonthReceived
            val totApp = if (item.occupancyTotalApplications > 0L) item.occupancyTotalApplications else item.totalReceived
            val inspBld = if (item.occupancyInspectedBuildings > 0L) item.occupancyInspectedBuildings else item.previousInspectionCount
            val row = listOf(
                (idx + 1).toString(),
                item.reportCategory.ifBlank { "ভবন পরিদর্শন সম্পর্কিত রিপোর্ট" },
                item.inspectionDate,
                item.ownerName,
                item.buildingAddress,
                item.mouzaName,
                item.thanaName,
                item.ownerMobile,
                item.buildingType,
                item.previousInspectionCount.toString(),
                authVisited,
                item.deviationType,
                item.actionTaken,
                item.previousMonthBalance.toString(),
                item.currentMonthReceived.toString(),
                item.totalReceived.toString(),
                item.unresolvedDurationCategory,
                item.planSubmitted.toString(),
                item.planApproved.toString(),
                item.planRejected.toString(),
                item.planCompleted.toString(),
                item.planWorkInProgress.toString(),
                item.planSentForFeedback.toString(),
                item.planCompletedWithin45Days.toString(),
                item.planCompletedMoreThan45Days.toString(),
                prevBal.toString(),
                currRec.toString(),
                totApp.toString(),
                inspBld.toString(),
                item.occupancyIssuedCount.toString(),
                item.occupancyNotIssuedCount.toString(),
                item.occupancyNotIssuedReason,
                item.magistrateName,
                item.mobileCourtDate,
                item.mobileCourtFineAmount.toString(),
                if (item.mobileCourtMeterCut) "হ্যাঁ" else "না",
                item.mobileCourtMeterCount.toString(),
                if (item.mobileCourtUndertakingSubmitted) "হ্যাঁ" else "না",
                item.remarks
            ).joinToString(",") { escapeCsv(it) }
            sb.appendLine(row)
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        val cleaned = value.replace("\"", "\"\"")
        return "\"$cleaned\""
    }

    fun copyReportToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Monthly Building Inspection Report", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "মাসিক রিপোর্ট ক্লিপবোর্ডে কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
    }

    fun shareTextReport(context: Context, reportMonth: String, text: String) {
        val monthBangla = InspectionPresets.formatReportMonthBangla(reportMonth)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "ইমারত পরিদর্শনের মাসিক রিপোর্ট - $monthBangla")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(sendIntent, "মাসিক রিপোর্ট শেয়ার করুন")
        context.startActivity(chooser)
    }

    fun shareCsvDataAsText(context: Context, reportMonth: String, reports: List<InspectionReport>) {
        val csv = buildCsvContent(reports)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Inspection_Report_$reportMonth.csv")
            putExtra(Intent.EXTRA_TEXT, csv)
        }
        val chooser = Intent.createChooser(sendIntent, "CSV / Excel ডাটা শেয়ার করুন")
        context.startActivity(chooser)
    }

    fun printOrSavePdfReport(
        context: Context,
        reportMonth: String,
        reports: List<InspectionReport>,
        profile: InspectorProfile?,
        stats: MonthlySummaryStats
    ) {
        printOrSaveHeadOfficePdfReport(
            context = context,
            reportMonth = reportMonth,
            reports = reports,
            profile = profile,
            stats = stats
        )
    }

    fun printOrSaveHeadOfficePdfReport(
        context: Context,
        reportMonth: String,
        reports: List<InspectionReport>,
        profile: InspectorProfile?,
        stats: MonthlySummaryStats,
        zoneName: String = "",
        subZoneName: String = ""
    ) {
        val (pdfBytes, pageCount) = HeadOfficePdfGenerator.generateHeadOfficeOfficialPdfBytes(
            reportMonth = reportMonth,
            reports = reports,
            profile = profile,
            stats = stats,
            zoneName = zoneName,
            subZoneName = subZoneName
        )

        dispatchPdfToPrintManager(
            context = context,
            jobName = "HeadOffice_Monthly_Report_$reportMonth",
            pdfBytes = pdfBytes,
            pageCount = pageCount,
            isPortrait = true
        )
    }

    fun shareHeadOfficePdfOnWhatsApp(
        context: Context,
        reportMonth: String,
        reports: List<InspectionReport>,
        profile: InspectorProfile?,
        stats: MonthlySummaryStats,
        zoneName: String = "",
        subZoneName: String = ""
    ) {
        val monthBangla = InspectionPresets.formatReportMonthBangla(reportMonth)
        val (pdfBytes, _) = HeadOfficePdfGenerator.generateHeadOfficeOfficialPdfBytes(
            reportMonth = reportMonth,
            reports = reports,
            profile = profile,
            stats = stats,
            zoneName = zoneName,
            subZoneName = subZoneName
        )
        savePdfToDownloads(context, "HeadOffice_Monthly_Report_$reportMonth", pdfBytes)
        HeadOfficePdfGenerator.sharePdfBytesOnWhatsApp(
            context = context,
            fileName = "HeadOffice_Monthly_Report_$reportMonth",
            shareTitle = "রাজউক মাসিক সমন্বয় সভার অগ্রগতির প্রতিবেদন ($monthBangla) - জোন: $zoneName, সাবজোন: $subZoneName",
            pdfBytes = pdfBytes
        )
    }

    /**
     * এডমিনের পছন্দমতো কলাম ও অপশন দিয়ে তৈরি কাস্টমাইজড পিডিএফ রিপোর্ট (Customized Report)
     */
    fun printOrSaveCustomizedPdfReport(
        context: Context,
        reportMonth: String,
        reports: List<InspectionReport>,
        profile: InspectorProfile?,
        customTitle: String,
        selectedCategory: String?,
        selectedColumns: Set<ReportColumn>
    ) {
        val (pdfBytes, pageCount) = generateCustomizedPdfBytes(
            reportMonth = reportMonth,
            reports = reports,
            profile = profile,
            customTitle = customTitle,
            selectedCategory = selectedCategory,
            selectedColumns = selectedColumns
        )

        dispatchPdfToPrintManager(
            context = context,
            jobName = "Customized_Report_$reportMonth",
            pdfBytes = pdfBytes,
            pageCount = pageCount,
            isPortrait = false
        )
    }

    fun shareCustomizedPdfOnWhatsApp(
        context: Context,
        reportMonth: String,
        reports: List<InspectionReport>,
        profile: InspectorProfile?,
        customTitle: String,
        selectedCategory: String?,
        selectedColumns: Set<ReportColumn>
    ) {
        val monthBangla = InspectionPresets.formatReportMonthBangla(reportMonth)
        val titleText = customTitle.ifBlank { "ইমারত পরিদর্শনের কাস্টমাইজড প্রতিবেদন" }
        val (pdfBytes, _) = generateCustomizedPdfBytes(
            reportMonth = reportMonth,
            reports = reports,
            profile = profile,
            customTitle = customTitle,
            selectedCategory = selectedCategory,
            selectedColumns = selectedColumns
        )
        savePdfToDownloads(context, "Customized_Report_$reportMonth", pdfBytes)
        HeadOfficePdfGenerator.sharePdfBytesOnWhatsApp(
            context = context,
            fileName = "Customized_Report_$reportMonth",
            shareTitle = "$titleText ($monthBangla)",
            pdfBytes = pdfBytes
        )
    }

    private fun generateCustomizedPdfBytes(
        reportMonth: String,
        reports: List<InspectionReport>,
        profile: InspectorProfile?,
        customTitle: String,
        selectedCategory: String?,
        selectedColumns: Set<ReportColumn>
    ): Pair<ByteArray, Int> {
        val monthBangla = InspectionPresets.formatReportMonthBangla(reportMonth)
        val inspector = profile?.inspectorName?.ifBlank { null }
            ?: reports.firstOrNull()?.inspectorName
            ?: "অ্যাডমিন / পরিদর্শক"
        val office = profile?.officeName?.takeIf { it.isNotBlank() } ?: "ইমারত পরিদর্শন শাখা, রাজউক"

        val filteredReports = if (!selectedCategory.isNullOrBlank() && selectedCategory != "সকল ক্যাটাগরি") {
            reports.filter { it.reportCategory.trim() == selectedCategory.trim() }
        } else {
            reports
        }

        val activeColumns = ReportColumn.entries.filter { it in selectedColumns }.ifEmpty {
            listOf(ReportColumn.SL, ReportColumn.OWNER_NAME, ReportColumn.BUILDING_ADDRESS, ReportColumn.ACTION_TAKEN)
        }

        val titleText = customTitle.ifBlank { "ইমারত পরিদর্শনের কাস্টমাইজড প্রতিবেদন" }
        val categoryLabel = if (!selectedCategory.isNullOrBlank() && selectedCategory != "সকল ক্যাটাগরি") {
            "ক্যাটাগরি: $selectedCategory"
        } else {
            "ক্যাটাগরি: সকল ক্যাটাগরি"
        }

        val headers = activeColumns.map { it.titleBangla.substringAfter(". ").trim() }
        val rawWeights = activeColumns.map { col ->
            when (col) {
                ReportColumn.SL -> 0.5f
                ReportColumn.INSPECTION_DATE, ReportColumn.OWNER_MOBILE,
                ReportColumn.PREV_INSPECTION_COUNT, ReportColumn.AUTH_OFFICER_INSPECTED -> 0.8f
                ReportColumn.BUILDING_ADDRESS, ReportColumn.DESIGN_APPROVAL,
                ReportColumn.OCCUPANCY_CERT, ReportColumn.MOBILE_COURT -> 1.4f
                else -> 1.0f
            }
        }
        val weightSum = rawWeights.sum().coerceAtLeast(1f)
        val weights = rawWeights.map { it / weightSum }

        val tableRows = filteredReports.mapIndexed { idx, r ->
            activeColumns.map { col ->
                when (col) {
                    ReportColumn.SL -> InspectionPresets.toBanglaDigits(idx + 1)
                    ReportColumn.CATEGORY -> r.reportCategory.ifBlank { "ভবন পরিদর্শন" }
                    ReportColumn.INSPECTION_DATE -> {
                        val d = if (InspectionPresets.isMobileCourtCategory(r.reportCategory) && r.mobileCourtDate.isNotBlank()) {
                            r.mobileCourtDate
                        } else {
                            r.inspectionDate
                        }
                        InspectionPresets.toBanglaDigits(d)
                    }
                    ReportColumn.OWNER_NAME -> r.ownerName.ifBlank { "-" }
                    ReportColumn.BUILDING_ADDRESS -> r.buildingAddress.ifBlank { "-" }
                    ReportColumn.MOUZA_NAME -> r.mouzaName.ifBlank { "-" }
                    ReportColumn.THANA_NAME -> r.thanaName.ifBlank { "-" }
                    ReportColumn.OWNER_MOBILE -> r.ownerMobile.ifBlank { "-" }
                    ReportColumn.BUILDING_TYPE -> r.buildingType.ifBlank { "-" }
                    ReportColumn.PREV_INSPECTION_COUNT -> "${InspectionPresets.toBanglaDigits(r.previousInspectionCount)} বার"
                    ReportColumn.AUTH_OFFICER_INSPECTED -> if (r.authorizedOfficerInspected) "হ্যাঁ" else "না"
                    ReportColumn.DEVIATION_TYPE -> r.deviationType.ifBlank { "-" }
                    ReportColumn.ACTION_TAKEN -> r.actionTaken.ifBlank { "-" }
                    ReportColumn.DESIGN_APPROVAL -> {
                        if (InspectionPresets.isDesignApprovalCategory(r.reportCategory)) {
                            "দাখিল: ${InspectionPresets.toBanglaDigits(r.planSubmitted)}, অনুমোদিত: ${InspectionPresets.toBanglaDigits(r.planApproved)}, প্রত্যাখ্যাত: ${InspectionPresets.toBanglaDigits(r.planRejected)}, সম্পন্ন: ${InspectionPresets.toBanglaDigits(r.planCompleted)}"
                        } else {
                            "-"
                        }
                    }
                    ReportColumn.OCCUPANCY_CERT -> {
                        if (InspectionPresets.isOccupancyCertificateCategory(r.reportCategory)) {
                            val prevBal = if (r.occupancyPreviousMonthBalance > 0L) r.occupancyPreviousMonthBalance else r.previousMonthBalance
                            val currRec = if (r.occupancyCurrentMonthReceived > 0L) r.occupancyCurrentMonthReceived else r.currentMonthReceived
                            "জের: ${InspectionPresets.toBanglaDigits(prevBal)}, প্রাপ্তি: ${InspectionPresets.toBanglaDigits(currRec)}, সনদ প্রদান: ${InspectionPresets.toBanglaDigits(r.occupancyIssuedCount)}, অপ্রদান: ${InspectionPresets.toBanglaDigits(r.occupancyNotIssuedCount)}"
                        } else {
                            "-"
                        }
                    }
                    ReportColumn.MOBILE_COURT -> {
                        if (InspectionPresets.isMobileCourtCategory(r.reportCategory)) {
                            val fine = "জরিমানা: ৳ ${InspectionPresets.toBanglaDigits(r.mobileCourtFineAmount)}"
                            val meter = if (r.mobileCourtMeterCut) "মিটার কর্তন: ${InspectionPresets.toBanglaDigits(r.mobileCourtMeterCount)}টি" else "মিটার কর্তন: না"
                            val under = if (r.mobileCourtUndertakingSubmitted) "অঙ্গীকার: দাখিল" else "অঙ্গীকার: না"
                            "$fine | $meter | $under"
                        } else {
                            "-"
                        }
                    }
                    ReportColumn.UNRESOLVED_DETAILS -> {
                        if (InspectionPresets.isUnresolvedCategory(r.reportCategory)) {
                            "জের: ${InspectionPresets.toBanglaDigits(r.previousMonthBalance)}, প্রাপ্তি: ${InspectionPresets.toBanglaDigits(r.currentMonthReceived)}, মাসান্তে অনিষ্পন্ন: ${InspectionPresets.toBanglaDigits(r.unresolvedAtMonthEnd)}"
                        } else {
                            "-"
                        }
                    }
                    ReportColumn.INSPECTOR_NAME -> r.inspectorName.ifBlank { "-" }
                    ReportColumn.REMARKS -> r.remarks.ifBlank { "-" }
                }
            }
        }

        val leftMeta = "শাখা/দপ্তর: $office\nপ্রস্তুতকারী: $inspector"
        val rightMeta = "নির্বাচিত কলাম সংখ্যা: ${InspectionPresets.toBanglaDigits(activeColumns.size)} টি\n" +
            "মোট রেকর্ড: ${InspectionPresets.toBanglaDigits(filteredReports.size)} টি"

        return renderNativePdfDocument(
            topGovLine = "রাজধানী উন্নয়ন কর্তৃপক্ষ (রাজউক)",
            mainTitle = titleText,
            subHeader = office,
            badgeText = "কাস্টমাইজড ফিল্টার ও কলাম ভিত্তিক প্রতিবেদন",
            infoLine = "প্রতিবেদনের মাস: $monthBangla | $categoryLabel",
            leftMeta = leftMeta,
            rightMeta = rightMeta,
            summaryBoxes = emptyList(),
            headers = headers,
            weights = weights,
            rows = tableRows
        )
    }

    private fun renderNativePdfDocument(
        topGovLine: String,
        mainTitle: String,
        subHeader: String,
        badgeText: String,
        infoLine: String,
        leftMeta: String,
        rightMeta: String,
        summaryBoxes: List<Pair<String, String>>,
        headers: List<String>,
        weights: List<Float>,
        rows: List<List<String>>
    ): Pair<ByteArray, Int> {
        val pageWidth = 842 // A4 Landscape width in points
        val pageHeight = 595 // A4 Landscape height in points
        val margin = 24f
        val contentWidth = pageWidth - (margin * 2)

        val pdfDocument = PdfDocument()
        var pageNumber = 1
        var currentPage = pdfDocument.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        )
        var canvas = currentPage.canvas
        var currentY = margin

        val primaryColor = Color.parseColor("#0B4F60")
        val darkTextColor = Color.parseColor("#132226")
        val mutedTextColor = Color.parseColor("#455A64")
        val borderColor = Color.parseColor("#6F848C")
        val metaBgColor = Color.parseColor("#F4F7F8")
        val metaBorderColor = Color.parseColor("#CDE9F0")
        val evenRowColor = Color.parseColor("#F8FAFB")

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            color = borderColor
        }

        val centerTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkTextColor
            textAlign = Paint.Align.CENTER
        }

        // 1. Draw Main Header on Page 1
        centerTextPaint.textSize = 9.5f
        centerTextPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(topGovLine, pageWidth / 2f, currentY + 10f, centerTextPaint)
        currentY += 15f

        centerTextPaint.textSize = 15f
        centerTextPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        centerTextPaint.color = primaryColor
        canvas.drawText(mainTitle, pageWidth / 2f, currentY + 13f, centerTextPaint)
        currentY += 18f

        centerTextPaint.textSize = 10f
        centerTextPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        centerTextPaint.color = darkTextColor
        canvas.drawText(subHeader, pageWidth / 2f, currentY + 10f, centerTextPaint)
        currentY += 14f

        // Badge Pill
        centerTextPaint.textSize = 9f
        centerTextPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val badgeWidth = centerTextPaint.measureText(badgeText) + 20f
        val badgeRect = RectF(
            (pageWidth - badgeWidth) / 2f,
            currentY,
            (pageWidth + badgeWidth) / 2f,
            currentY + 16f
        )
        fillPaint.color = primaryColor
        canvas.drawRoundRect(badgeRect, 4f, 4f, fillPaint)
        centerTextPaint.color = Color.WHITE
        canvas.drawText(badgeText, pageWidth / 2f, currentY + 11.5f, centerTextPaint)
        currentY += 20f

        // Info line
        centerTextPaint.textSize = 9.5f
        centerTextPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        centerTextPaint.color = darkTextColor
        canvas.drawText(infoLine, pageWidth / 2f, currentY + 10f, centerTextPaint)
        currentY += 15f

        // Header bottom divider
        strokePaint.color = primaryColor
        strokePaint.strokeWidth = 1.5f
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, strokePaint)
        currentY += 8f

        // 2. Meta Row Box
        val metaTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkTextColor
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val leftLayout = buildStaticLayout(leftMeta, metaTextPaint, (contentWidth * 0.55f).toInt())
        val rightLayout = buildStaticLayout(rightMeta, metaTextPaint, (contentWidth * 0.40f).toInt())
        val metaBoxHeight = maxOf(leftLayout.height, rightLayout.height) + 12f
        val metaRect = RectF(margin, currentY, pageWidth - margin, currentY + metaBoxHeight)
        fillPaint.color = metaBgColor
        canvas.drawRoundRect(metaRect, 4f, 4f, fillPaint)
        strokePaint.color = metaBorderColor
        strokePaint.strokeWidth = 0.8f
        canvas.drawRoundRect(metaRect, 4f, 4f, strokePaint)

        canvas.save()
        canvas.translate(margin + 8f, currentY + 6f)
        leftLayout.draw(canvas)
        canvas.restore()

        canvas.save()
        canvas.translate(margin + contentWidth * 0.58f, currentY + 6f)
        rightLayout.draw(canvas)
        canvas.restore()

        currentY += metaBoxHeight + 8f

        // 3. Optional Summary Boxes
        if (summaryBoxes.isNotEmpty()) {
            val gap = 6f
            val boxWidth = (contentWidth - gap * (summaryBoxes.size - 1)) / summaryBoxes.size
            val boxHeight = 28f
            summaryBoxes.forEachIndexed { i, (valText, labelText) ->
                val bx = margin + i * (boxWidth + gap)
                val bRect = RectF(bx, currentY, bx + boxWidth, currentY + boxHeight)
                fillPaint.color = Color.WHITE
                canvas.drawRoundRect(bRect, 4f, 4f, fillPaint)
                strokePaint.color = borderColor
                strokePaint.strokeWidth = 0.6f
                canvas.drawRoundRect(bRect, 4f, 4f, strokePaint)

                centerTextPaint.textSize = 9.5f
                centerTextPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                centerTextPaint.color = primaryColor
                canvas.drawText(valText, bx + boxWidth / 2f, currentY + 12f, centerTextPaint)

                centerTextPaint.textSize = 7.5f
                centerTextPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                centerTextPaint.color = mutedTextColor
                canvas.drawText(labelText, bx + boxWidth / 2f, currentY + 23f, centerTextPaint)
            }
            currentY += boxHeight + 8f
        }

        // Column widths in points
        val colWidths = weights.map { it * contentWidth }

        val headerTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val cellTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkTextColor
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        fun drawTableHeader(yStart: Float): Float {
            val layouts = headers.mapIndexed { idx, h ->
                buildStaticLayout(
                    text = h,
                    paint = headerTextPaint,
                    width = (colWidths[idx] - 6f).toInt().coerceAtLeast(16),
                    alignment = Layout.Alignment.ALIGN_CENTER
                )
            }
            val hHeight = (layouts.maxOfOrNull { it.height } ?: 14) + 8f
            var xPos = margin
            headers.indices.forEach { idx ->
                val cw = colWidths[idx]
                val cellRect = RectF(xPos, yStart, xPos + cw, yStart + hHeight)
                fillPaint.color = primaryColor
                canvas.drawRect(cellRect, fillPaint)
                strokePaint.color = borderColor
                strokePaint.strokeWidth = 0.6f
                canvas.drawRect(cellRect, strokePaint)

                canvas.save()
                canvas.translate(xPos + 3f, yStart + 4f)
                layouts[idx].draw(canvas)
                canvas.restore()

                xPos += cw
            }
            return yStart + hHeight
        }

        currentY = drawTableHeader(currentY)

        // 4. Draw Table Rows with pagination (no signature footer at bottom)
        if (rows.isEmpty()) {
            val emptyHeight = 24f
            val rect = RectF(margin, currentY, pageWidth - margin, currentY + emptyHeight)
            strokePaint.color = borderColor
            canvas.drawRect(rect, strokePaint)
            centerTextPaint.textSize = 8.5f
            centerTextPaint.color = mutedTextColor
            canvas.drawText("এই মাসে কোনো রেকর্ড পাওয়া যায়নি", pageWidth / 2f, currentY + 15f, centerTextPaint)
            currentY += emptyHeight
        } else {
            rows.forEachIndexed { rowIndex, rowCells ->
                val cellLayouts = rowCells.mapIndexed { colIdx, cellStr ->
                    val cw = colWidths.getOrElse(colIdx) { 40f }
                    val align = if (colIdx == 0) Layout.Alignment.ALIGN_CENTER else Layout.Alignment.ALIGN_NORMAL
                    buildStaticLayout(
                        text = cellStr,
                        paint = cellTextPaint,
                        width = (cw - 6f).toInt().coerceAtLeast(16),
                        alignment = align
                    )
                }
                val rowHeight = (cellLayouts.maxOfOrNull { it.height } ?: 12) + 8f

                if (currentY + rowHeight > pageHeight - margin) {
                    pdfDocument.finishPage(currentPage)
                    pageNumber++
                    currentPage = pdfDocument.startPage(
                        PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    )
                    canvas = currentPage.canvas
                    currentY = margin
                    currentY = drawTableHeader(currentY)
                }

                var xPos = margin
                rowCells.indices.forEach { colIdx ->
                    val cw = colWidths.getOrElse(colIdx) { 40f }
                    val cellRect = RectF(xPos, currentY, xPos + cw, currentY + rowHeight)
                    if (rowIndex % 2 == 1) {
                        fillPaint.color = evenRowColor
                        canvas.drawRect(cellRect, fillPaint)
                    }
                    strokePaint.color = borderColor
                    strokePaint.strokeWidth = 0.5f
                    canvas.drawRect(cellRect, strokePaint)

                    canvas.save()
                    canvas.translate(xPos + 3f, currentY + 4f)
                    cellLayouts[colIdx].draw(canvas)
                    canvas.restore()

                    xPos += cw
                }
                currentY += rowHeight
            }
        }

        pdfDocument.finishPage(currentPage)
        val out = ByteArrayOutputStream()
        pdfDocument.writeTo(out)
        pdfDocument.close()
        return out.toByteArray() to pageNumber
    }

    private fun buildStaticLayout(
        text: String,
        paint: TextPaint,
        width: Int,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
    ): StaticLayout {
        val safeWidth = width.coerceAtLeast(16)
        return StaticLayout.Builder
            .obtain(text, 0, text.length, paint, safeWidth)
            .setAlignment(alignment)
            .setLineSpacing(0f, 1.05f)
            .setIncludePad(false)
            .build()
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    private fun savePdfToDownloads(
        context: Context,
        fileName: String,
        pdfBytes: ByteArray
    ): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.pdf")
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        out.write(pdfBytes)
                        out.flush()
                    }
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun dispatchPdfToPrintManager(
        context: Context,
        jobName: String,
        pdfBytes: ByteArray,
        pageCount: Int,
        isPortrait: Boolean = false
    ) {
        val savedToDownloads = savePdfToDownloads(context, jobName, pdfBytes)
        if (savedToDownloads) {
            Toast.makeText(
                context,
                "পিডিএফ ফাইল Downloads ফোল্ডারে সংরক্ষিত হয়েছে ($jobName.pdf)",
                Toast.LENGTH_LONG
            ).show()
        }

        try {
            val activityContext = context.findActivity() ?: context
            val printManager = activityContext.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                if (!savedToDownloads) {
                    Toast.makeText(context, "প্রিন্ট সার্ভিস পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                }
                return
            }

            val printAdapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback.onLayoutCancelled()
                        return
                    }
                    val info = PrintDocumentInfo.Builder("$jobName.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(pageCount)
                        .build()
                    callback.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback.onWriteCancelled()
                        return
                    }
                    try {
                        FileOutputStream(destination.fileDescriptor).use { outStream ->
                            outStream.write(pdfBytes)
                            outStream.flush()
                        }
                        callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback.onWriteFailed(e.localizedMessage ?: "PDF Write Error")
                    }
                }
            }

            val mediaSize = if (isPortrait) {
                PrintAttributes.MediaSize.ISO_A4.asPortrait()
            } else {
                PrintAttributes.MediaSize.ISO_A4.asLandscape()
            }

            val attributes = PrintAttributes.Builder()
                .setMediaSize(mediaSize)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()

            printManager.print(jobName, printAdapter, attributes)
        } catch (e: Exception) {
            if (!savedToDownloads) {
                Toast.makeText(
                    context,
                    "পিডিএফ প্রিন্ট শুরু করতে সমস্যা হয়েছে: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
