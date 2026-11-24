package com.mindsparq.os

import com.mindsparq.os.core.auth.AppDestination
import com.mindsparq.os.core.auth.RoleCode
import com.mindsparq.os.core.auth.allowedDestinations
import com.mindsparq.os.core.i18n.formatNpr
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CoreTests {
    @Test
    fun nprFormattingFollowsNepaleseGrouping() {
        assertEquals("रू 500.00", formatNpr(500.0))
        assertEquals("रू 1,25,000.00", formatNpr(125000.0))
        assertEquals("-रू 12,34,567.50", formatNpr(-1234567.5))
    }

    @Test
    fun teacherCannotAccessFinanceOrInventory() {
        val allowed = allowedDestinations(setOf(RoleCode.TEACHER), isDesktop = true)
        assertFalse(AppDestination.FINANCE in allowed)
        assertFalse(AppDestination.INVENTORY in allowed)
        assertTrue(AppDestination.FIELD_OPS in allowed)
        assertTrue(AppDestination.SCHOOLS in allowed)
    }

    @Test
    fun financeOfficerCanAccessFinanceOnDesktop() {
        val allowed = allowedDestinations(setOf(RoleCode.FINANCE), isDesktop = true)
        assertTrue(AppDestination.FINANCE in allowed)
        assertTrue(AppDestination.REPORTS in allowed)
    }
}
