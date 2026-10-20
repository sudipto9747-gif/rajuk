package com.example

import com.example.data.model.InspectionPresets
import com.example.data.model.InspectionReport
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun buildingTypes_hasExactlyConstructedAndUnderConstruction() {
        assertEquals(
            listOf("নির্মিত", "নির্মাণাধীন"),
            InspectionPresets.buildingTypes
        )
        assertTrue(
            InspectionPresets.deviationTypesFor("নির্মিত")
                .contains("অনুমোদিত নকশা বহির্ভূত নির্মিত")
        )
        assertTrue(
            InspectionPresets.deviationTypesFor("নির্মাণাধীন")
                .contains("অনুমোদিত নকশা বহির্ভূত নির্মাণ")
        )
        assertTrue(
            InspectionPresets.actionTakenTypesFor("নির্মাণাধীন")
                .contains("কাজ বন্ধের নির্দেশ ও নোটিশ জারি")
        )
    }

    @Test
    fun computeStats_countsConstructedAndUnderConstructionSeparately() {
        val reports = listOf(
            InspectionReport(buildingType = "নির্মিত", authorizedOfficerInspected = true, previousInspectionCount = 1L),
            InspectionReport(buildingType = "নির্মাণাধীন", authorizedOfficerInspected = false, previousInspectionCount = 2L),
            InspectionReport(buildingType = "নির্মাণাধীন", authorizedOfficerInspected = true, previousInspectionCount = 0L)
        )
        val stats = InspectionPresets.computeStats(reports)
        assertEquals(3, stats.totalInspections)
        assertEquals(1, stats.constructedBuildingCount)
        assertEquals(2, stats.underConstructionBuildingCount)
    }

    @Test
    fun designApprovalCategory_andFields_areValid() {
        assertTrue(InspectionPresets.isDesignApprovalCategory("নকশা অনুমোদন"))
        assertTrue(InspectionPresets.isDesignApprovalCategory("নকশা অনুমোদনের তথ্য"))
        assertFalse(InspectionPresets.isDesignApprovalCategory("ভবন পরিদর্শন সম্পর্কিত রিপোর্ট"))
        assertFalse(InspectionPresets.isDesignApprovalCategory("অনিষ্পন্ন রিপোর্ট"))

        val report = InspectionReport(
            reportCategory = "নকশা অনুমোদন",
            planSubmitted = 25L,
            planApproved = 15L,
            planRejected = 3L,
            planSentForFeedback = 2L,
            planCompleted = 20L, // 15 + 3 + 2
            planWorkInProgress = 5L,
            planCompletedWithin45Days = 14L,
            planCompletedMoreThan45Days = 6L,
            remarks = "নকশা অনুমোদন অক্টোবর ২০২৬"
        )

        assertEquals(25L, report.planSubmitted)
        assertEquals(15L, report.planApproved)
        assertEquals(3L, report.planRejected)
        assertEquals(2L, report.planSentForFeedback)
        assertEquals(20L, report.planCompleted)
        assertEquals(5L, report.planWorkInProgress)
        assertEquals(14L, report.planCompletedWithin45Days)
        assertEquals(6L, report.planCompletedMoreThan45Days)
        assertEquals(
            report.planApproved + report.planRejected + report.planSentForFeedback,
            report.planCompleted
        )
    }
}

