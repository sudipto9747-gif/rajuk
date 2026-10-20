package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.InspectionPresets
import com.example.data.model.InspectionReport
import com.example.data.model.InspectorProfile
import com.example.data.model.MonthlySummaryStats
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * রাজউক প্রধান কার্যালয়ের (Head Office) মাসিক সমন্বয় সভার অফিসিয়াল ৫-টেবিল ফরম্যাট পিডিএফ জেনারেটর
 * এবং সরাসরি WhatsApp-এ পিডিএফ ফাইল শেয়ারিং ইউটিলিটি।
 */
object HeadOfficePdfGenerator {

    private data class HeaderCellSpec(
        val text: String,
        val startCol: Int,
        val colSpan: Int = 1,
        val startRow: Int = 0, // 0 or 1
        val rowSpan: Int = 1   // 1 or 2
    )

    private fun toBanglaTwoDigits(value: Long): String {
        val raw = if (value in 0L..9L) "0$value" else value.toString()
        return InspectionPresets.toBanglaDigits(raw)
    }

    private fun toBanglaTwoDigits(value: Int): String = toBanglaTwoDigits(value.toLong())

    private fun shortZoneLabel(raw: String): String {
        val cleaned = raw.trim()
            .removePrefix("জোন-")
            .removePrefix("জোন")
            .trim()
        return InspectionPresets.toBanglaDigits(cleaned.ifBlank { "১" })
    }

    private fun parseSubZoneLabels(raw: String): List<String> {
        val parts = raw.split(",", "،")
            .map { part ->
                InspectionPresets.toBanglaDigits(
                    part.trim()
                        .removePrefix("সাবজোন-")
                        .removePrefix("সাব-জোন-")
                        .removePrefix("সাবজোন")
                        .removePrefix("সাব জোন")
                        .trim()
                )
            }
            .filter { it.isNotBlank() }
        return parts.ifEmpty { listOf("২", "৩") }
    }

    private fun buildStaticLayout(
        text: String,
        paint: TextPaint,
        width: Int,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_CENTER
    ): StaticLayout {
        val safeWidth = width.coerceAtLeast(10)
        return StaticLayout.Builder
            .obtain(text, 0, text.length, paint, safeWidth)
            .setAlignment(alignment)
            .setLineSpacing(0f, 1.0f)
            .setIncludePad(false)
            .build()
    }

    /**
     * অফিসিয়াল রাজউক ফরম্যাটে ৫টি ছকসহ (নিষ্পত্তি সংক্রান্ত, নির্মাণাধীন ভবন পরিদর্শন,
     * নকশা অনুমোদন, ব্যবহার সনদ/Occupancy Certificate এবং মোবাইল কোর্ট) পিডিএফ তৈরি করে।
     */
    fun generateHeadOfficeOfficialPdfBytes(
        reportMonth: String,
        reports: List<InspectionReport>,
        profile: InspectorProfile?,
        stats: MonthlySummaryStats,
        zoneName: String,
        subZoneName: String
    ): Pair<ByteArray, Int> {
        val pageWidth = 595 // A4 Portrait width in points
        val pageHeight = 842 // A4 Portrait height in points
        val margin = 20f
        val contentWidth = pageWidth - (margin * 2)

        val zoneShort = shortZoneLabel(zoneName)
        val subZones = parseSubZoneLabels(subZoneName)

        val pdfDocument = PdfDocument()
        var pageNumber = 1
        var currentPage = pdfDocument.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        )
        var canvas = currentPage.canvas
        var currentY = margin

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.75f
            color = Color.BLACK
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.BLACK
        }

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val sectionTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textAlign = Paint.Align.LEFT
            textSize = 7.8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val tableHeaderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 6.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val colNumberPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 6.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val cellTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 6.4f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val boldCellTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 6.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        fun ensureSpace(neededHeight: Float) {
            if (currentY + neededHeight > pageHeight - margin) {
                pdfDocument.finishPage(currentPage)
                pageNumber++
                currentPage = pdfDocument.startPage(
                    PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                )
                canvas = currentPage.canvas
                currentY = margin
            }
        }

        fun drawUnderlinedCenteredText(text: String, y: Float, textSize: Float) {
            titlePaint.textSize = textSize
            val centerX = pageWidth / 2f
            canvas.drawText(text, centerX, y, titlePaint)
            val textWidth = titlePaint.measureText(text)
            canvas.drawLine(
                centerX - textWidth / 2f,
                y + 1.8f,
                centerX + textWidth / 2f,
                y + 1.8f,
                borderPaint
            )
        }

        // ==================== TOP OFFICIAL HEADER ====================
        titlePaint.textSize = 9.5f
        canvas.drawText("রাজধানী উন্নয়ন কর্তৃপক্ষ", pageWidth / 2f, currentY + 9f, titlePaint)
        currentY += 12.5f

        drawUnderlinedCenteredText("রাজউক ভবন, ঢাকা।", currentY + 9f, 9f)
        currentY += 15f

        drawUnderlinedCenteredText(
            "কর্তৃপক্ষের মাসিক সমন্বয় সভার সিদ্ধান্তের উন্নয়ন নিয়ন্ত্রণ শাখা সংশ্লিষ্ট বাস্তবায়ন অগ্রগতির প্রতিবেদন:",
            currentY + 9f,
            8.8f
        )
        currentY += 16f

        fun drawSectionHeading(title: String) {
            ensureSpace(45f)
            // Small black square bullet ■
            val sqSize = 4.5f
            canvas.drawRect(margin, currentY + 2.5f, margin + sqSize, currentY + 2.5f + sqSize, fillPaint)
            canvas.drawText(title, margin + sqSize + 4f, currentY + 7.2f, sectionTitlePaint)
            currentY += 10.5f
        }

        fun drawTextInBox(
            c: Canvas,
            text: String,
            paint: TextPaint,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
            align: Layout.Alignment = Layout.Alignment.ALIGN_CENTER
        ) {
            if (text.isEmpty()) return
            val boxW = (right - left - 3f).toInt().coerceAtLeast(8)
            val layout = buildStaticLayout(text, paint, boxW, align)
            val textY = top + ((bottom - top) - layout.height) / 2f
            c.save()
            c.translate(left + 1.5f, textY.coerceAtLeast(top + 1f))
            layout.draw(c)
            c.restore()
        }

        fun drawMultiRowHeaderAndIndexRow(
            colWeights: List<Float>,
            headerSpecs: List<HeaderCellSpec>,
            isTwoRowHeader: Boolean
        ): List<Float> {
            val colWidths = colWeights.map { it * contentWidth }
            val colStarts = FloatArray(colWidths.size)
            var acc = margin
            for (i in colWidths.indices) {
                colStarts[i] = acc
                acc += colWidths[i]
            }

            // Compute required heights for header rows
            var row0H = 13f
            var row1H = if (isTwoRowHeader) 18f else 0f

            headerSpecs.forEach { spec ->
                val w = colWidths.subList(spec.startCol, spec.startCol + spec.colSpan).sum()
                val layout = buildStaticLayout(spec.text, tableHeaderPaint, (w - 3f).toInt().coerceAtLeast(8))
                val needed = layout.height + 5f
                if (!isTwoRowHeader || spec.rowSpan == 2) {
                    if (!isTwoRowHeader) {
                        row0H = maxOf(row0H, needed)
                    } else if (needed > row0H + row1H) {
                        row1H = needed - row0H
                    }
                } else if (spec.startRow == 0) {
                    row0H = maxOf(row0H, needed)
                } else {
                    row1H = maxOf(row1H, needed)
                }
            }

            val totalHeaderH = row0H + row1H
            val indexRowH = 11f
            ensureSpace(totalHeaderH + indexRowH + 16f)

            // Draw header cells
            headerSpecs.forEach { spec ->
                val left = colStarts[spec.startCol]
                val width = colWidths.subList(spec.startCol, spec.startCol + spec.colSpan).sum()
                val right = left + width
                val top = currentY + (if (spec.startRow == 0) 0f else row0H)
                val bottom = top + when {
                    !isTwoRowHeader -> row0H
                    spec.rowSpan == 2 -> totalHeaderH
                    spec.startRow == 0 -> row0H
                    else -> row1H
                }
                canvas.drawRect(left, top, right, bottom, borderPaint)
                drawTextInBox(canvas, spec.text, tableHeaderPaint, left, top, right, bottom)
            }
            currentY += totalHeaderH

            // Draw Column Index Row (১, ২, ৩...)
            for (i in colWidths.indices) {
                val left = colStarts[i]
                val right = left + colWidths[i]
                canvas.drawRect(left, currentY, right, currentY + indexRowH, borderPaint)
                drawTextInBox(
                    canvas,
                    InspectionPresets.toBanglaDigits(i + 1),
                    colNumberPaint,
                    left,
                    currentY,
                    right,
                    currentY + indexRowH
                )
            }
            currentY += indexRowH
            return colWidths
        }

        fun drawSubZoneTableRows(
            colWidths: List<Float>,
            subZoneRows: List<List<String>>,
            totalRow: List<String>? = null
        ) {
            val colStarts = FloatArray(colWidths.size)
            var acc = margin
            for (i in colWidths.indices) {
                colStarts[i] = acc
                acc += colWidths[i]
            }

            // Compute heights for each subZoneRow
            val rowHeights = subZoneRows.map { cells ->
                var maxH = 12.5f
                for (cIdx in 1 until cells.size) {
                    val w = (colWidths[cIdx] - 3f).toInt().coerceAtLeast(8)
                    val l = buildStaticLayout(cells[cIdx], cellTextPaint, w)
                    maxH = maxOf(maxH, l.height + 4f)
                }
                maxH
            }

            val totalDataHeight = rowHeights.sum()
            val totalRowHeight = if (totalRow != null) 13f else 0f
            ensureSpace(totalDataHeight + totalRowHeight + 6f)

            val dataTopY = currentY
            subZoneRows.forEachIndexed { rIdx, cells ->
                val rH = rowHeights[rIdx]
                for (cIdx in 1 until colWidths.size) {
                    val left = colStarts[cIdx]
                    val right = left + colWidths[cIdx]
                    canvas.drawRect(left, currentY, right, currentY + rH, borderPaint)
                    drawTextInBox(
                        canvas,
                        cells.getOrElse(cIdx) { "" },
                        cellTextPaint,
                        left,
                        currentY,
                        right,
                        currentY + rH
                    )
                }
                currentY += rH
            }

            // Draw merged Col 0 (জোন) across all subZoneRows
            val zoneLeft = colStarts[0]
            val zoneRight = zoneLeft + colWidths[0]
            canvas.drawRect(zoneLeft, dataTopY, zoneRight, currentY, borderPaint)
            drawTextInBox(
                canvas,
                subZoneRows.firstOrNull()?.firstOrNull() ?: zoneShort,
                cellTextPaint,
                zoneLeft,
                dataTopY,
                zoneRight,
                currentY
            )

            // Draw optional মোট (Total) bottom row
            if (totalRow != null) {
                val rH = 13f
                for (cIdx in colWidths.indices) {
                    val left = colStarts[cIdx]
                    val right = left + colWidths[cIdx]
                    canvas.drawRect(left, currentY, right, currentY + rH, borderPaint)
                    drawTextInBox(
                        canvas,
                        totalRow.getOrElse(cIdx) { "" },
                        boldCellTextPaint,
                        left,
                        currentY,
                        right,
                        currentY + rH
                    )
                }
                currentY += rH
            }
            currentY += 6f
        }

        // Helper to filter reports by subzone if multiple subzones exist
        fun reportsForSubZone(source: List<InspectionReport>, index: Int, szLabel: String): List<InspectionReport> {
            if (subZones.size == 1) return source
            val matched = source.filter {
                it.remarks.contains(szLabel) || it.mouzaName.contains(szLabel) || it.thanaName.contains(szLabel)
            }
            return if (matched.isNotEmpty()) {
                matched
            } else if (index == 0) {
                source
            } else {
                emptyList()
            }
        }

        // ==================== ১. নিষ্পত্তি সংক্রান্ত প্রতিবেদন ====================
        drawSectionHeading("নিষ্পত্তি সংক্রান্ত প্রতিবেদন:")
        val t1Weights = listOf(0.055f, 0.065f, 0.085f, 0.11f, 0.07f, 0.095f, 0.125f, 0.125f, 0.085f, 0.065f, 0.12f)
        val t1Specs = listOf(
            HeaderCellSpec("জোন", 0, 1, 0, 2),
            HeaderCellSpec("সাবজোন", 1, 1, 0, 2),
            HeaderCellSpec("গত মাসের জের", 2, 1, 0, 2),
            HeaderCellSpec("আলোচ্য মাসে প্রাপ্তি", 3, 1, 0, 2),
            HeaderCellSpec("মোট প্রাপ্তি", 4, 1, 0, 2),
            HeaderCellSpec("আলোচ্য মাসে নিষ্পত্তি", 5, 1, 0, 2),
            HeaderCellSpec("মাসের শেষ কার্য দিবসে অনিষ্পন্ন", 6, 4, 0, 1),
            HeaderCellSpec("এক সপ্তাহের অধিক অথচ দুই সপ্তাহের বেশি নহে", 6, 1, 1, 1),
            HeaderCellSpec("দুই সপ্তাহের অধিক অথচ এক মাসের বেশি নহে", 7, 1, 1, 1),
            HeaderCellSpec("এক মাসের অধিক", 8, 1, 1, 1),
            HeaderCellSpec("মোট", 9, 1, 1, 1),
            HeaderCellSpec("মন্তব্য", 10, 1, 0, 2)
        )
        val t1ColWidths = drawMultiRowHeaderAndIndexRow(t1Weights, t1Specs, isTwoRowHeader = true)
        val unresolvedAll = reports.filter { InspectionPresets.isUnresolvedCategory(it.reportCategory) }
        val t1Rows = subZones.mapIndexed { idx, sz ->
            val subReports = reportsForSubZone(unresolvedAll, idx, sz)
            val prevBal = subReports.sumOf { it.previousMonthBalance }
            val currRec = subReports.sumOf { it.currentMonthReceived }
            val totRec = subReports.sumOf {
                if (it.totalReceived > 0L) it.totalReceived else (it.previousMonthBalance + it.currentMonthReceived)
            }
            val unres1to2W = subReports.filter {
                it.unresolvedDurationCategory.contains("১ সপ্তাহ") || it.unresolvedDurationCategory.contains("এক সপ্তাহ")
            }.sumOf { it.unresolvedAtMonthEnd }
            val unres2Wto1M = subReports.filter {
                it.unresolvedDurationCategory.contains("২ সপ্তাহ") || it.unresolvedDurationCategory.contains("দুই সপ্তাহ")
            }.sumOf { it.unresolvedAtMonthEnd }
            val unresOver1M = subReports.filter {
                val c = it.unresolvedDurationCategory
                !c.contains("১ সপ্তাহ") && !c.contains("এক সপ্তাহ") && !c.contains("২ সপ্তাহ") && !c.contains("দুই সপ্তাহ")
            }.sumOf { it.unresolvedAtMonthEnd }
            val unresTotal = unres1to2W + unres2Wto1M + unresOver1M
            val resolvedThisMonth = (totRec - unresTotal).coerceAtLeast(0L)
            val rem = subReports.mapNotNull { it.remarks.trim().takeIf { s -> s.isNotEmpty() } }
                .distinct().joinToString(", ").ifBlank { "০০" }

            listOf(
                zoneShort,
                sz,
                toBanglaTwoDigits(prevBal),
                toBanglaTwoDigits(currRec),
                toBanglaTwoDigits(totRec),
                toBanglaTwoDigits(resolvedThisMonth),
                toBanglaTwoDigits(unres1to2W),
                toBanglaTwoDigits(unres2Wto1M),
                toBanglaTwoDigits(unresOver1M),
                toBanglaTwoDigits(unresTotal),
                rem
            )
        }
        drawSubZoneTableRows(t1ColWidths, t1Rows, totalRow = null)

        // ==================== ২. নির্মাণাধীন ভবন পরিদর্শনের তথ্য ====================
        drawSectionHeading("নির্মাণাধীন ভবন পরিদর্শনের তথ্য:")
        val t2Weights = listOf(
            0.045f, 0.045f, 0.115f, 0.085f, 0.075f, 0.06f, 0.065f,
            0.105f, 0.065f, 0.065f, 0.065f, 0.07f, 0.065f, 0.075f
        )
        val t2Specs = listOf(
            HeaderCellSpec("জোন", 0, 1, 0, 2),
            HeaderCellSpec("সাব জোন", 1, 1, 0, 2),
            HeaderCellSpec("ইমারত পরিদর্শকের নাম", 2, 1, 0, 2),
            HeaderCellSpec("ইমারত পরিদর্শক কর্তৃক পরিদর্শনকৃত ভবনের সংখ্যা", 3, 1, 0, 2),
            HeaderCellSpec("সর্বমোট পরিদর্শনের সংখ্যা", 4, 1, 0, 2),
            HeaderCellSpec("নির্মিত ভবন", 5, 1, 0, 2),
            HeaderCellSpec("নির্মাণাধীন ভবন", 6, 1, 0, 2),
            HeaderCellSpec("অথরাইজড অফিসার কর্তৃক ভবন পরিদর্শন করা হয়েছে (ভবনের সংখ্যা)", 7, 1, 0, 2),
            HeaderCellSpec("ব্যত্যয়কৃত ভবনের সংখ্যা", 8, 2, 0, 1),
            HeaderCellSpec("নির্মিত ভবন", 8, 1, 1, 1),
            HeaderCellSpec("নির্মাণাধীন ভবন", 9, 1, 1, 1),
            HeaderCellSpec("গৃহীত ব্যবস্থা (নির্মিত ভবন)", 10, 2, 0, 1),
            HeaderCellSpec("নোটিশ", 10, 1, 1, 1),
            HeaderCellSpec("মোবাইল কোর্ট", 11, 1, 1, 1),
            HeaderCellSpec("গৃহীত ব্যবস্থা (নির্মাণাধীন ভবন)", 12, 2, 0, 1),
            HeaderCellSpec("নোটিশ", 12, 1, 1, 1),
            HeaderCellSpec("মোবাইল কোর্ট", 13, 1, 1, 1)
        )
        val t2ColWidths = drawMultiRowHeaderAndIndexRow(t2Weights, t2Specs, isTwoRowHeader = true)
        val buildingAll = reports.filter { InspectionPresets.isBuildingInspectionCategory(it.reportCategory) }

        // Group by subzone -> inspector rows
        data class InspectorRowData(val subZone: String, val cells: List<String>)
        val t2Groups = subZones.mapIndexed { idx, sz ->
            val subReports = reportsForSubZone(buildingAll, idx, sz)
            val groupedByInspector = subReports.groupBy {
                it.inspectorName.trim().ifBlank {
                    profile?.inspectorName?.takeIf { n -> n.isNotBlank() } ?: "ইমারত পরিদর্শক"
                }
            }
            val rowsForSz = if (groupedByInspector.isEmpty()) {
                val defaultName = if (idx == 0) {
                    profile?.inspectorName?.takeIf { it.isNotBlank() } ?: "নির্ধারিত নয়"
                } else {
                    "-"
                }
                listOf(
                    InspectorRowData(
                        subZone = sz,
                        cells = listOf(
                            zoneShort, sz, defaultName,
                            "০০", "০০", "০০", "০০", "০০", "০০", "০০", "০০", "০০", "০০", "০০"
                        )
                    )
                )
            } else {
                groupedByInspector.map { (inspName, inspReports) ->
                    val bldCount = inspReports.size.toLong()
                    val totalVisitCount = inspReports.sumOf { 1L + it.previousInspectionCount }
                    val constructed = inspReports.filter { it.buildingType.trim() == InspectionPresets.BUILDING_TYPE_CONSTRUCTED }
                    val underConst = inspReports.filter { it.buildingType.trim() == InspectionPresets.BUILDING_TYPE_UNDER_CONSTRUCTION }
                    val authVisited = inspReports.count { it.authorizedOfficerInspected }.toLong()

                    val devConstructed = constructed.count {
                        it.deviationType.isNotBlank() && !it.deviationType.contains("ব্যত্যয় নেই")
                    }.toLong()
                    val devUnderConst = underConst.count {
                        it.deviationType.isNotBlank() && !it.deviationType.contains("ব্যত্যয় নেই")
                    }.toLong()

                    val noticeConstructed = constructed.count { it.actionTaken.contains("নোটিশ") }.toLong()
                    val mcConstructed = constructed.count {
                        it.actionTaken.contains("মোবাইল কোর্ট") || it.actionTaken.contains("উচ্ছেদ") || it.actionTaken.contains("মামলা")
                    }.toLong()

                    val noticeUnderConst = underConst.count { it.actionTaken.contains("নোটিশ") }.toLong()
                    val mcUnderConst = underConst.count {
                        it.actionTaken.contains("মোবাইল কোর্ট") || it.actionTaken.contains("উচ্ছেদ") || it.actionTaken.contains("মামলা") || it.actionTaken.contains("জব্দ")
                    }.toLong()

                    InspectorRowData(
                        subZone = sz,
                        cells = listOf(
                            zoneShort,
                            sz,
                            inspName,
                            toBanglaTwoDigits(bldCount),
                            toBanglaTwoDigits(totalVisitCount),
                            toBanglaTwoDigits(constructed.size),
                            toBanglaTwoDigits(underConst.size),
                            toBanglaTwoDigits(authVisited),
                            toBanglaTwoDigits(devConstructed),
                            toBanglaTwoDigits(devUnderConst),
                            toBanglaTwoDigits(noticeConstructed),
                            toBanglaTwoDigits(mcConstructed),
                            toBanglaTwoDigits(noticeUnderConst),
                            toBanglaTwoDigits(mcUnderConst)
                        )
                    )
                }
            }
            sz to rowsForSz
        }

        // Draw Table 2 rows with merged Col 0 (Zone) and merged Col 1 (Subzone per group)
        val t2Starts = FloatArray(t2ColWidths.size)
        var t2Acc = margin
        for (i in t2ColWidths.indices) {
            t2Starts[i] = t2Acc
            t2Acc += t2ColWidths[i]
        }
        val t2TopY = currentY
        for ((szLabel, groupRows) in t2Groups) {
            val szTopY = currentY
            for (row in groupRows) {
                val nameW = (t2ColWidths[2] - 3f).toInt().coerceAtLeast(10)
                val nameLayout = buildStaticLayout(row.cells[2], cellTextPaint, nameW)
                val rH = maxOf(13.5f, nameLayout.height + 4f)
                for (cIdx in 2 until t2ColWidths.size) {
                    val left = t2Starts[cIdx]
                    val right = left + t2ColWidths[cIdx]
                    canvas.drawRect(left, currentY, right, currentY + rH, borderPaint)
                    drawTextInBox(canvas, row.cells[cIdx], cellTextPaint, left, currentY, right, currentY + rH)
                }
                currentY += rH
            }
            // Merged Subzone cell (Col 1)
            canvas.drawRect(t2Starts[1], szTopY, t2Starts[1] + t2ColWidths[1], currentY, borderPaint)
            drawTextInBox(canvas, szLabel, cellTextPaint, t2Starts[1], szTopY, t2Starts[1] + t2ColWidths[1], currentY)
        }
        // Merged Zone cell (Col 0) across all groups in Table 2
        canvas.drawRect(t2Starts[0], t2TopY, t2Starts[0] + t2ColWidths[0], currentY, borderPaint)
        drawTextInBox(canvas, zoneShort, cellTextPaint, t2Starts[0], t2TopY, t2Starts[0] + t2ColWidths[0], currentY)
        currentY += 6f

        // ==================== ৩. নকশা অনুমোদনের তথ্য ====================
        drawSectionHeading("নকশা অনুমোদনের তথ্য:")
        val t3Weights = listOf(0.06f, 0.07f, 0.11f, 0.15f, 0.12f, 0.12f, 0.11f, 0.12f, 0.14f)
        val t3Specs = listOf(
            HeaderCellSpec("জোন", 0),
            HeaderCellSpec("সাবজোন", 1),
            HeaderCellSpec("পূর্ববর্তী জের", 2),
            HeaderCellSpec("আলোচ্য মাসে জমাকৃত নকশার সংখ্যা", 3),
            HeaderCellSpec("অনুমোদিত নকশার সংখ্যা", 4),
            HeaderCellSpec("প্রত্যাখ্যানযোগ্য নকশার সংখ্যা", 5),
            HeaderCellSpec("আর্কাইভকৃত নথির সংখ্যা", 6),
            HeaderCellSpec("অনিষ্পন্ন নকশার সংখ্যা", 7),
            HeaderCellSpec("আবেদনকারীর নিকট অপেক্ষমান (Resubmit)", 8)
        )
        val t3ColWidths = drawMultiRowHeaderAndIndexRow(t3Weights, t3Specs, isTwoRowHeader = false)
        val designAll = reports.filter { InspectionPresets.isDesignApprovalCategory(it.reportCategory) }
        var t3SumPrev = 0L
        var t3SumSub = 0L
        var t3SumApp = 0L
        var t3SumRej = 0L
        var t3SumArch = 0L
        var t3SumWip = 0L
        var t3SumResub = 0L

        val t3Rows = subZones.mapIndexed { idx, sz ->
            val subReports = reportsForSubZone(designAll, idx, sz)
            val prev = subReports.sumOf { it.previousMonthBalance }
            val sub = subReports.sumOf { it.planSubmitted }
            val app = subReports.sumOf { it.planApproved }
            val rej = subReports.sumOf { it.planRejected }
            val arch = subReports.sumOf { it.planCompleted }
            val wip = subReports.sumOf { it.planWorkInProgress }
            val resub = subReports.sumOf { it.planSentForFeedback }

            t3SumPrev += prev
            t3SumSub += sub
            t3SumApp += app
            t3SumRej += rej
            t3SumArch += arch
            t3SumWip += wip
            t3SumResub += resub

            listOf(
                zoneShort,
                sz,
                toBanglaTwoDigits(prev),
                toBanglaTwoDigits(sub),
                toBanglaTwoDigits(app),
                toBanglaTwoDigits(rej),
                toBanglaTwoDigits(arch),
                toBanglaTwoDigits(wip),
                toBanglaTwoDigits(resub)
            )
        }
        val t3TotalRow = listOf(
            "মোট",
            "",
            toBanglaTwoDigits(t3SumPrev),
            toBanglaTwoDigits(t3SumSub),
            toBanglaTwoDigits(t3SumApp),
            toBanglaTwoDigits(t3SumRej),
            toBanglaTwoDigits(t3SumArch),
            toBanglaTwoDigits(t3SumWip),
            toBanglaTwoDigits(t3SumResub)
        )
        drawSubZoneTableRows(t3ColWidths, t3Rows, totalRow = t3TotalRow)

        // ==================== ৪. ব্যবহার সনদের (Occupancy Certificate) তথ্য ====================
        drawSectionHeading("ব্যবহার সনদের (Occupancy Certificate) তথ্য:")
        val t4Weights = listOf(0.055f, 0.065f, 0.09f, 0.11f, 0.09f, 0.09f, 0.13f, 0.13f, 0.14f, 0.10f)
        val t4Specs = listOf(
            HeaderCellSpec("জোন", 0),
            HeaderCellSpec("সাবজোন", 1),
            HeaderCellSpec("পূর্ববর্তী মাসের জের", 2),
            HeaderCellSpec("বর্তমান মাসে প্রাপ্ত আবেদনের সংখ্যা", 3),
            HeaderCellSpec("জেরসহ মোট আবেদন সংখ্যা", 4),
            HeaderCellSpec("পরিদর্শনকৃত ভবনের সংখ্যা", 5),
            HeaderCellSpec("কতটি Occupancy Certificate প্রদান করা হয়েছে", 6),
            HeaderCellSpec("কতটি Occupancy Certificate প্রদান করা হয় নি", 7),
            HeaderCellSpec("কতটি Occupancy Certificate প্রদান না করার কারণ", 8),
            HeaderCellSpec("মন্তব্য", 9)
        )
        val t4ColWidths = drawMultiRowHeaderAndIndexRow(t4Weights, t4Specs, isTwoRowHeader = false)
        val occupancyAll = reports.filter { InspectionPresets.isOccupancyCertificateCategory(it.reportCategory) }
        var t4SumPrev = 0L
        var t4SumCurr = 0L
        var t4SumTot = 0L
        var t4SumInsp = 0L
        var t4SumIssued = 0L
        var t4SumNotIssued = 0L

        val t4Rows = subZones.mapIndexed { idx, sz ->
            val subReports = reportsForSubZone(occupancyAll, idx, sz)
            val prev = subReports.sumOf {
                if (it.occupancyPreviousMonthBalance > 0L) it.occupancyPreviousMonthBalance else it.previousMonthBalance
            }
            val curr = subReports.sumOf {
                if (it.occupancyCurrentMonthReceived > 0L) it.occupancyCurrentMonthReceived else it.currentMonthReceived
            }
            val tot = subReports.sumOf {
                if (it.occupancyTotalApplications > 0L) it.occupancyTotalApplications else (prev + curr)
            }
            val insp = subReports.sumOf {
                if (it.occupancyInspectedBuildings > 0L) it.occupancyInspectedBuildings else it.previousInspectionCount
            }
            val issued = subReports.sumOf { it.occupancyIssuedCount }
            val notIssued = subReports.sumOf { it.occupancyNotIssuedCount }
            val reason = subReports.mapNotNull { it.occupancyNotIssuedReason.trim().takeIf { s -> s.isNotEmpty() } }
                .distinct().joinToString(", ").ifBlank { "০০" }
            val rem = subReports.mapNotNull { it.remarks.trim().takeIf { s -> s.isNotEmpty() } }
                .distinct().joinToString(", ").ifBlank { "০০" }

            t4SumPrev += prev
            t4SumCurr += curr
            t4SumTot += tot
            t4SumInsp += insp
            t4SumIssued += issued
            t4SumNotIssued += notIssued

            listOf(
                zoneShort,
                sz,
                toBanglaTwoDigits(prev),
                toBanglaTwoDigits(curr),
                toBanglaTwoDigits(tot),
                toBanglaTwoDigits(insp),
                toBanglaTwoDigits(issued),
                toBanglaTwoDigits(notIssued),
                reason,
                rem
            )
        }
        val t4TotalRow = listOf(
            "মোট",
            "",
            toBanglaTwoDigits(t4SumPrev),
            toBanglaTwoDigits(t4SumCurr),
            toBanglaTwoDigits(t4SumTot),
            toBanglaTwoDigits(t4SumInsp),
            toBanglaTwoDigits(t4SumIssued),
            toBanglaTwoDigits(t4SumNotIssued),
            "০০",
            "০০"
        )
        drawSubZoneTableRows(t4ColWidths, t4Rows, totalRow = t4TotalRow)

        // ==================== ৫. মোবাইল কোর্টের তথ্য ====================
        drawSectionHeading("মোবাইল কোর্টের তথ্য:")
        val t5Weights = listOf(0.06f, 0.07f, 0.14f, 0.095f, 0.095f, 0.13f, 0.14f, 0.13f, 0.14f)
        val t5Specs = listOf(
            HeaderCellSpec("জোন", 0, 1, 0, 2),
            HeaderCellSpec("সাবজোন", 1, 1, 0, 2),
            HeaderCellSpec("পরিচালিত মোবাইল কোর্ট সংখ্যা", 2, 1, 0, 2),
            HeaderCellSpec("ভবনের সংখ্যা", 3, 2, 0, 1),
            HeaderCellSpec("নির্মিত", 3, 1, 1, 1),
            HeaderCellSpec("নির্মাণাধীন", 4, 1, 1, 1),
            HeaderCellSpec("জরিমানার পরিমাণ (টাকা)", 5, 1, 0, 2),
            HeaderCellSpec("বৈদ্যুতিক সংযোগ বিচ্ছিন্নকৃত ভবনের সংখ্যা", 6, 1, 0, 2),
            HeaderCellSpec("বৈদ্যুতিক মিটার কর্তনের সংখ্যা", 7, 1, 0, 2),
            HeaderCellSpec("মন্তব্য", 8, 1, 0, 2)
        )
        val t5ColWidths = drawMultiRowHeaderAndIndexRow(t5Weights, t5Specs, isTwoRowHeader = true)
        val mcAll = reports.filter { InspectionPresets.isMobileCourtCategory(it.reportCategory) }
        var t5SumCourts = 0L
        var t5SumConst = 0L
        var t5SumUnderConst = 0L
        var t5SumFine = 0L
        var t5SumDiscBld = 0L
        var t5SumMeters = 0L

        val t5Rows = subZones.mapIndexed { idx, sz ->
            val subReports = reportsForSubZone(mcAll, idx, sz)
            val courtCount = subReports.size.toLong()
            val constCount = subReports.count { it.buildingType.trim() == InspectionPresets.BUILDING_TYPE_CONSTRUCTED }.toLong()
            val underConstCount = subReports.count { it.buildingType.trim() == InspectionPresets.BUILDING_TYPE_UNDER_CONSTRUCTION }.toLong()
            val fine = subReports.sumOf { it.mobileCourtFineAmount }
            val discBld = subReports.count { it.mobileCourtMeterCut }.toLong()
            val meters = subReports.sumOf { it.mobileCourtMeterCount }
            val rem = subReports.mapNotNull { it.remarks.trim().takeIf { s -> s.isNotEmpty() } }
                .distinct().joinToString(", ")

            t5SumCourts += courtCount
            t5SumConst += constCount
            t5SumUnderConst += underConstCount
            t5SumFine += fine
            t5SumDiscBld += discBld
            t5SumMeters += meters

            listOf(
                zoneShort,
                sz,
                toBanglaTwoDigits(courtCount),
                toBanglaTwoDigits(constCount),
                toBanglaTwoDigits(underConstCount),
                toBanglaTwoDigits(fine),
                toBanglaTwoDigits(discBld),
                toBanglaTwoDigits(meters),
                rem
            )
        }
        val t5TotalRow = listOf(
            "মোট",
            "",
            toBanglaTwoDigits(t5SumCourts),
            toBanglaTwoDigits(t5SumConst),
            toBanglaTwoDigits(t5SumUnderConst),
            toBanglaTwoDigits(t5SumFine),
            toBanglaTwoDigits(t5SumDiscBld),
            toBanglaTwoDigits(t5SumMeters),
            ""
        )
        drawSubZoneTableRows(t5ColWidths, t5Rows, totalRow = t5TotalRow)

        pdfDocument.finishPage(currentPage)
        val out = ByteArrayOutputStream()
        pdfDocument.writeTo(out)
        pdfDocument.close()
        return out.toByteArray() to pageNumber
    }

    /**
     * পিডিএফ ফাইলটি ক্যাশ ফোল্ডারে সংরক্ষণ করে সরাসরি WhatsApp-এ (অথবা শেয়ার শিট-এ) পাঠায়।
     */
    fun sharePdfBytesOnWhatsApp(
        context: Context,
        fileName: String,
        shareTitle: String,
        pdfBytes: ByteArray
    ) {
        try {
            val pdfDir = File(context.cacheDir, "pdfs")
            if (!pdfDir.exists()) {
                pdfDir.mkdirs()
            }
            val safeName = fileName.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            val pdfFile = File(pdfDir, "$safeName.pdf")
            FileOutputStream(pdfFile).use { out ->
                out.write(pdfBytes)
                out.flush()
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val whatsappPackage = findInstalledWhatsAppPackage(context.packageManager)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, shareTitle)
                putExtra(Intent.EXTRA_TEXT, shareTitle)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = android.content.ClipData.newRawUri(shareTitle, contentUri)
            }

            if (whatsappPackage != null) {
                shareIntent.setPackage(whatsappPackage)
                context.startActivity(shareIntent)
            } else {
                val chooser = Intent.createChooser(shareIntent, "WhatsApp / অন্য মাধ্যমে পিডিএফ শেয়ার করুন")
                chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "পিডিএফ শেয়ার করতে সমস্যা হয়েছে: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun findInstalledWhatsAppPackage(pm: PackageManager): String? {
        val candidates = listOf("com.whatsapp", "com.whatsapp.w4b")
        for (pkg in candidates) {
            try {
                pm.getPackageInfo(pkg, 0)
                return pkg
            } catch (_: Exception) {
                // Not installed
            }
        }
        return null
    }
}
