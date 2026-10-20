package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.security.MessageDigest

const val BOOTSTRAPPED_ADMIN_EMAIL = "sudipto9747@gmail.com"
const val ADMIN_LOGIN_PHONE = "01767563095"
const val ADMIN_LOGIN_PASSWORD_HASH = "8f7d4d88e66e3b732938d940368541407d91c2c3d24fc7b73ab99e4594bcd4ae"

object PasswordHasher {
    fun sha256Hex(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.trim().toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun isAdminCredentials(phoneNumber: String, rawPassword: String): Boolean {
        return phoneNumber.trim() == ADMIN_LOGIN_PHONE &&
            sha256Hex(rawPassword) == ADMIN_LOGIN_PASSWORD_HASH
    }

    fun phoneToUserId(phoneNumber: String): String {
        val clean = phoneNumber.trim().filter { it.isLetterOrDigit() }
        return "phone_${clean.ifBlank { "user" }}"
    }
}

@IgnoreExtraProperties
data class UserAccount(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val inspectorName: String = "",
    val designation: String = "",
    val officeName: String = "",
    val phoneNumber: String = "",
    val passwordHash: String = "",
    val approvalStatus: String = "pending", // "pending", "approved", "rejected"
    val role: String = "inspector", // "inspector", "admin"
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class InspectorProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val inspectorName: String = "",
    val designation: String = "",
    val officeName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AppOption(
    @DocumentId val id: String = "",
    val group: String = "", // "category", "building_type", "deviation_type", "action_taken", "unresolved_duration"
    val name: String = "",
    val subGroup: String = "", // "constructed", "under_construction", or "general"
    val sortOrder: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AssignedTask(
    @DocumentId val id: String = "",
    val title: String = "", // কাজের শিরোনাম
    val description: String = "", // কাজের বিবরণ ও নির্দেশনা
    val assignedToUserId: String = "", // কার প্রতি এসাইন করা হয়েছে
    val assignedToName: String = "", // পরিদর্শকের নাম
    val assignedToDesignation: String = "", // পদবী
    val assignedToPhone: String = "", // ফোন নাম্বার
    val createdByUserId: String = "", // অ্যাডমিনের আইডি
    val createdByName: String = "", // অ্যাডমিনের নাম
    val dueDate: String = "", // শেষ সময়সীমা (YYYY-MM-DD)
    val priority: String = "Medium", // "High" (জরুরি), "Medium" (সাধারণ), "Low" (কম জরুরি)
    val status: String = "pending", // "pending" (অপেক্ষমাণ), "in_progress" (চলমান), "completed" (সম্পন্ন)
    val completionNote: String = "", // কাজ শেষে পরিদর্শকের মন্তব্য
    val buildingAddress: String = "", // সংশ্লিষ্ট ভবনের ঠিকানা / এলাকা (ঐচ্ছিক)
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class InspectionReport(
    @DocumentId val id: String = "",
    val userId: String = "",
    val reportCategory: String = "ভবন পরিদর্শন সম্পর্কিত রিপোর্ট", // ৫টি অপশনের যেকোনো একটি
    val reportMonth: String = "", // Format: YYYY-MM (e.g., "2026-10")
    val inspectionDate: String = "", // Format: YYYY-MM-DD (e.g., "2026-10-19")
    val inspectorName: String = "", // ইমারত পরিদর্শকের নাম (স্বয়ংক্রিয় প্রোফাইল হতে)
    val ownerName: String = "", // ভবন মালিকের নাম
    val buildingAddress: String = "", // ভবনের ঠিকানা
    val mouzaName: String = "", // মৌজার নাম
    val thanaName: String = "", // থানার নাম
    val ownerMobile: String = "", // মালিকের মোবাইল নাম্বার
    val buildingType: String = "", // ৫. ভবনের ধরণ
    val previousInspectionCount: Long = 0L, // ৬. আগে কতবার পরিদর্শন করা হয়েছে
    val authorizedOfficerInspected: Boolean = false, // ৭. অথরাইজড অফিসার পরিদর্শন করেছে ?
    val deviationType: String = "", // ৮. ব্যত্যয়কৃত ভবনের ধরণ
    val actionTaken: String = "", // ৯. গৃহীত ব্যবস্থা
    // অনিষ্পন্ন রিপোর্টের ৫টি ঘরের তথ্য (Unresolved Report Fields):
    val previousMonthBalance: Long = 0L, // ১. গত মাসের জের
    val currentMonthReceived: Long = 0L, // ২. আলোচ্য মাসের প্রাপ্তি
    val totalReceived: Long = 0L, // ৩. মোট প্রাপ্তি
    val unresolvedAtMonthEnd: Long = 0L, // ৪. মাসের শেষ কার্যদিবসে অনিষ্পন্ন (সংখ্যা)
    val unresolvedDurationCategory: String = "", // ৪. মাসের শেষ কার্যদিবসে অনিষ্পন্ন (৩টি অপশনের যেকোনো ১টি)
    // নকশা অনুমোদনের তথ্য (Design Approval Fields):
    val planSubmitted: Long = 0L, // 1. Submitted
    val planApproved: Long = 0L, // 2. Approved
    val planRejected: Long = 0L, // 3. Rejected
    val planCompleted: Long = 0L, // 4. Completed (Approved + Rejected + Sent for Applicants Feedback)
    val planWorkInProgress: Long = 0L, // 5. Work In Progress
    val planSentForFeedback: Long = 0L, // 6. Sent For Applicants Feedback
    val planCompletedWithin45Days: Long = 0L, // 7. Completed Within 45 Working Days
    val planCompletedMoreThan45Days: Long = 0L, // 8. Completed In More Than 45 Working Days
    // অকুপেন্সী সনদের তথ্য (Occupancy Certificate Fields):
    val occupancyPreviousMonthBalance: Long = 0L, // ১. পূর্ববর্তী মাসের জের
    val occupancyCurrentMonthReceived: Long = 0L, // ২. বর্তমান মাসে প্রাপ্ত আবেদনের সংখ্যা
    val occupancyTotalApplications: Long = 0L, // ৩. জেরসহ মোট আবেদন সংখ্যা
    val occupancyInspectedBuildings: Long = 0L, // ৪. পরিদর্শনকৃত ভবনের সংখ্যা
    val occupancyIssuedCount: Long = 0L, // ৫. কতটি Occupancy Certificate প্রদান করা হয়েছে
    val occupancyNotIssuedCount: Long = 0L, // ৬. কতটি Occupancy Certificate প্রদান করা হয় নি
    val occupancyNotIssuedReason: String = "", // ৭. Occupancy Certificate প্রদান না করার কারণ
    // মোবাইল কোর্টের তথ্য (Mobile Court Fields):
    val mobileCourtDate: String = "", // মোবাইল কোর্টের তারিখ
    val magistrateName: String = "", // ম্যাজিস্ট্রেট এর নাম
    val mobileCourtFineAmount: Long = 0L, // ভবন মালিক কে কত টাকা জরিমানা করা হয়েছে (টাকা)
    val mobileCourtMeterCut: Boolean = false, // মিটার কাটা হয়েছে কিনা?
    val mobileCourtMeterCount: Long = 0L, // হলে কতটি মিটার কাটা হয়েছে
    val mobileCourtUndertakingSubmitted: Boolean = false, // ভবন মালিক অঙ্গীকার নামা দাখিল করেছে কিনা
    val remarks: String = "", // মন্তব্য / অতিরিক্ত মন্তব্য
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class InspectionFormData(
    val reportCategory: String = "ভবন পরিদর্শন সম্পর্কিত রিপোর্ট",
    val reportMonth: String = "",
    val inspectionDate: String = "",
    val inspectorName: String = "",
    val ownerName: String = "",
    val buildingAddress: String = "",
    val mouzaName: String = "",
    val thanaName: String = "",
    val ownerMobile: String = "",
    val buildingType: String = InspectionPresets.BUILDING_TYPE_CONSTRUCTED,
    val previousInspectionCount: Long = 0L,
    val authorizedOfficerInspected: Boolean = false,
    val deviationType: String = "",
    val actionTaken: String = "",
    val previousMonthBalance: Long = 0L,
    val currentMonthReceived: Long = 0L,
    val totalReceived: Long = 0L,
    val unresolvedAtMonthEnd: Long = 0L,
    val unresolvedDurationCategory: String = "",
    val planSubmitted: Long = 0L,
    val planApproved: Long = 0L,
    val planRejected: Long = 0L,
    val planCompleted: Long = 0L,
    val planWorkInProgress: Long = 0L,
    val planSentForFeedback: Long = 0L,
    val planCompletedWithin45Days: Long = 0L,
    val planCompletedMoreThan45Days: Long = 0L,
    val occupancyPreviousMonthBalance: Long = 0L,
    val occupancyCurrentMonthReceived: Long = 0L,
    val occupancyTotalApplications: Long = 0L,
    val occupancyInspectedBuildings: Long = 0L,
    val occupancyIssuedCount: Long = 0L,
    val occupancyNotIssuedCount: Long = 0L,
    val occupancyNotIssuedReason: String = "",
    val mobileCourtDate: String = "",
    val magistrateName: String = "",
    val mobileCourtFineAmount: Long = 0L,
    val mobileCourtMeterCut: Boolean = false,
    val mobileCourtMeterCount: Long = 0L,
    val mobileCourtUndertakingSubmitted: Boolean = false,
    val remarks: String = ""
)

data class MonthlySummaryStats(
    val totalInspections: Int = 0,
    val authorizedOfficerVisitedCount: Int = 0,
    val deviatedBuildingCount: Int = 0,
    val repeatInspectionCount: Int = 0,
    val constructedBuildingCount: Int = 0,
    val underConstructionBuildingCount: Int = 0,
    val buildingTypeBreakdown: Map<String, Int> = emptyMap(),
    val deviationBreakdown: Map<String, Int> = emptyMap(),
    val actionBreakdown: Map<String, Int> = emptyMap()
)

object InspectionPresets {
    const val BUILDING_TYPE_CONSTRUCTED = "নির্মিত"
    const val BUILDING_TYPE_UNDER_CONSTRUCTION = "নির্মাণাধীন"

    val enterDataCategories = listOf(
        "অনিষ্পন্ন রিপোর্ট",
        "ভবন পরিদর্শন সম্পর্কিত রিপোর্ট",
        "নকশা অনুমোদন",
        "অকুপেন্সী সনদ",
        "মোবাইল কোর্টের তথ্য"
    )

    fun isUnresolvedCategory(category: String): Boolean =
        category.trim() == "অনিষ্পন্ন রিপোর্ট"

    fun isDesignApprovalCategory(category: String): Boolean {
        val trimmed = category.trim()
        return trimmed == "নকশা অনুমোদন" || trimmed.startsWith("নকশা অনুমোদন")
    }

    fun isOccupancyCertificateCategory(category: String): Boolean {
        val trimmed = category.trim()
        return trimmed == "অকুপেন্সী সনদ" || trimmed.startsWith("অকুপেন্সী সনদ")
    }

    fun isMobileCourtCategory(category: String): Boolean {
        val trimmed = category.trim()
        return trimmed == "মোবাইল কোর্টের তথ্য" || trimmed == "মোবাইল কোর্ট" || trimmed.startsWith("মোবাইল কোর্ট")
    }

    fun isBuildingInspectionCategory(category: String): Boolean =
        !isUnresolvedCategory(category) && 
        !isDesignApprovalCategory(category) && 
        !isOccupancyCertificateCategory(category) && 
        !isMobileCourtCategory(category)

    val unresolvedDurationOptions = listOf(
        "১ সপ্তাহের অধিক অথচ ২ সপ্তাহের অধিক নয়",
        "২ সপ্তাহের অধিক অথচ ১ মাসের অধিক নয়",
        "এক মাসের অধিক"
    )

    // ৫. ভবনের ধরণ হবে ২ প্রকার: নির্মিত ও নির্মাণাধীন
    val buildingTypes = listOf(
        BUILDING_TYPE_CONSTRUCTED,
        BUILDING_TYPE_UNDER_CONSTRUCTION
    )

    val deviationTypesConstructed = listOf(
        "অনুমোদিত নকশা বহির্ভূত নির্মিত",
        "অনুমোদনহীন নির্মিত (নকশা ব্যতীত)",
        "আবাসিক নকশায় বাণিজ্যিক ব্যবহার",
        "সেটব্যাক (Setback) লঙ্ঘন",
        "অতিরিক্ত তলা নির্মাণ",
        "অকুপেন্সী সনদ ব্যতীত ব্যবহার",
        "ঝুঁকিপূর্ণ / জরাজীর্ণ ভবন",
        "ব্যত্যয় নেই (নকশা অনুযায়ী)",
        "অন্যান্য"
    )

    val deviationTypesUnderConstruction = listOf(
        "অনুমোদিত নকশা বহির্ভূত নির্মাণ",
        "অনুমোদনহীন নির্মাণ (নকশা ব্যতীত)",
        "সেটব্যাক (Setback) লঙ্ঘন",
        "অতিরিক্ত তলা নির্মাণ চলমান",
        "নিরাপত্তা বেষ্টনী ব্যতীত নির্মাণ",
        "ব্যত্যয় নেই (নকশা অনুযায়ী)",
        "অন্যান্য"
    )

    val deviationTypes = (deviationTypesUnderConstruction + deviationTypesConstructed).distinct()

    val actionTakenTypesConstructed = listOf(
        "কারণ দর্শানোর নোটিশ প্রদান",
        "চূড়ান্ত নোটিশ প্রদান (ধারা ৩বি)",
        "উচ্ছেদ কার্যক্রম প্রস্তাব প্রেরণ",
        "ফৌজদারি / ইমারত আদালতে মামলা দায়ের",
        "বৈদ্যুতিক ও সেবা সংযোগ বিচ্ছিন্নকরণ প্রস্তাব",
        "অবৈধ ব্যবহার বন্ধের নোটিশ জারি",
        "সংশোধিত নকশা দাখিলের নির্দেশ",
        "নিয়মিত পর্যবেক্ষণ চলমান"
    )

    val actionTakenTypesUnderConstruction = listOf(
        "কাজ বন্ধের নির্দেশ ও নোটিশ জারি",
        "কারণ দর্শানোর নোটিশ প্রদান",
        "চূড়ান্ত নোটিশ প্রদান (ধারা ৩বি)",
        "নির্মাণ সামগ্রী জব্দ ও কাজ বন্ধকরণ",
        "উচ্ছেদ কার্যক্রম প্রস্তাব প্রেরণ",
        "ফৌজদারি / ইমারত আদালতে মামলা দায়ের",
        "বৈদ্যুতিক ও সেবা সংযোগ বিচ্ছিন্নকরণ প্রস্তাব",
        "সংশোধিত নকশা দাখিলের নির্দেশ",
        "নিয়মিত পর্যবেক্ষণ চলমান"
    )

    val actionTakenTypes = (actionTakenTypesConstructed + actionTakenTypesUnderConstruction).distinct()

    fun deviationTypesFor(buildingType: String): List<String> {
        return if (buildingType.trim() == BUILDING_TYPE_UNDER_CONSTRUCTION) {
            deviationTypesUnderConstruction
        } else {
            deviationTypesConstructed
        }
    }

    fun actionTakenTypesFor(buildingType: String): List<String> {
        return if (buildingType.trim() == BUILDING_TYPE_UNDER_CONSTRUCTION) {
            actionTakenTypesUnderConstruction
        } else {
            actionTakenTypesConstructed
        }
    }

    // Dynamic resolution based on Admin-configured AppOptions
    fun getDynamicCategories(customOptions: List<AppOption>): List<String> {
        val customs = customOptions.filter { it.group == "category" }
            .map { it.name.trim() }.filter { it.isNotBlank() }
        return if (customs.isNotEmpty()) customs else enterDataCategories
    }

    fun getDynamicBuildingTypes(customOptions: List<AppOption>): List<String> {
        val customs = customOptions.filter { it.group == "building_type" }
            .map { it.name.trim() }.filter { it.isNotBlank() }
        return if (customs.isNotEmpty()) customs else buildingTypes
    }

    fun getDynamicDeviationTypes(buildingType: String, customOptions: List<AppOption>): List<String> {
        val sub = if (buildingType.trim() == BUILDING_TYPE_UNDER_CONSTRUCTION) "under_construction" else "constructed"
        val customs = customOptions.filter { it.group == "deviation_type" && (it.subGroup.isBlank() || it.subGroup == sub || it.subGroup == "general") }
            .map { it.name.trim() }.filter { it.isNotBlank() }
        return if (customs.isNotEmpty()) customs else deviationTypesFor(buildingType)
    }

    fun getDynamicActionTakenTypes(buildingType: String, customOptions: List<AppOption>): List<String> {
        val sub = if (buildingType.trim() == BUILDING_TYPE_UNDER_CONSTRUCTION) "under_construction" else "constructed"
        val customs = customOptions.filter { it.group == "action_taken" && (it.subGroup.isBlank() || it.subGroup == sub || it.subGroup == "general") }
            .map { it.name.trim() }.filter { it.isNotBlank() }
        return if (customs.isNotEmpty()) customs else actionTakenTypesFor(buildingType)
    }

    fun getDynamicUnresolvedDurations(customOptions: List<AppOption>): List<String> {
        val customs = customOptions.filter { it.group == "unresolved_duration" }
            .map { it.name.trim() }.filter { it.isNotBlank() }
        return if (customs.isNotEmpty()) customs else unresolvedDurationOptions
    }

    val banglaMonths = mapOf(
        "01" to "জানুয়ারি",
        "02" to "ফেব্রুয়ারি",
        "03" to "মার্চ",
        "04" to "এপ্রিল",
        "05" to "মে",
        "06" to "জুন",
        "07" to "জুলাই",
        "08" to "আগস্ট",
        "09" to "সেপ্টেম্বর",
        "10" to "অক্টোবর",
        "11" to "নভেম্বর",
        "12" to "ডিসেম্বর"
    )

    fun formatReportMonthBangla(reportMonth: String): String {
        val parts = reportMonth.split("-")
        if (parts.size != 2) return reportMonth
        val year = toBanglaDigits(parts[0])
        val monthName = banglaMonths[parts[1]] ?: parts[1]
        return "$monthName $year"
    }

    fun toBanglaDigits(input: String): String {
        val en = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val bn = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder(input.length)
        for (ch in input) {
            val idx = en.indexOf(ch)
            if (idx >= 0) sb.append(bn[idx]) else sb.append(ch)
        }
        return sb.toString()
    }

    fun toBanglaDigits(number: Long): String = toBanglaDigits(number.toString())
    fun toBanglaDigits(number: Int): String = toBanglaDigits(number.toString())

    fun computeStats(reports: List<InspectionReport>): MonthlySummaryStats {
        val total = reports.size
        val authVisited = reports.count { it.authorizedOfficerInspected }
        val deviated = reports.count {
            it.deviationType.isNotBlank() && !it.deviationType.contains("ব্যত্যয় নেই")
        }
        val repeatCount = reports.count { it.previousInspectionCount > 0L }
        val constructedCount = reports.count { it.buildingType.trim() == BUILDING_TYPE_CONSTRUCTED }
        val underConstructionCount = reports.count { it.buildingType.trim() == BUILDING_TYPE_UNDER_CONSTRUCTION }
        val byBuildingType = reports.groupingBy { it.buildingType.ifBlank { BUILDING_TYPE_CONSTRUCTED } }.eachCount()
        val byDeviation = reports.groupingBy { it.deviationType.ifBlank { "অনির্ধারিত" } }.eachCount()
        val byAction = reports.groupingBy { it.actionTaken.ifBlank { "অনির্ধারিত" } }.eachCount()

        return MonthlySummaryStats(
            totalInspections = total,
            authorizedOfficerVisitedCount = authVisited,
            deviatedBuildingCount = deviated,
            repeatInspectionCount = repeatCount,
            constructedBuildingCount = constructedCount,
            underConstructionBuildingCount = underConstructionCount,
            buildingTypeBreakdown = byBuildingType,
            deviationBreakdown = byDeviation,
            actionBreakdown = byAction
        )
    }
}
