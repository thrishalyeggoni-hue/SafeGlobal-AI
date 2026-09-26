package com.example

import com.example.data.model.JourneyRecord
import com.example.data.model.JourneyStatus
import com.example.data.model.RiskState
import com.example.data.model.SafeSphereUser
import com.example.data.model.SafeZone
import com.example.data.model.TravelMode
import com.example.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeSphereUnitTest {

    @Test
    fun userModel_initialization_isCorrect() {
        val user = SafeSphereUser(
            id = "u1",
            displayName = "Sarah",
            role = UserRole.PARENT
        )
        assertEquals("Sarah", user.displayName)
        assertEquals(UserRole.PARENT, user.role)
        assertTrue(user.isPhoneVerified)
    }

    @Test
    fun journeyModel_activeState_isCorrect() {
        val journey = JourneyRecord(
            studentName = "Alex",
            origin = "School",
            destination = "Home",
            travelMode = TravelMode.CAR,
            status = JourneyStatus.ACTIVE,
            riskState = RiskState.NORMAL,
            batteryPercent = 84
        )
        assertEquals("Alex", journey.studentName)
        assertEquals(JourneyStatus.ACTIVE, journey.status)
        assertEquals(RiskState.NORMAL, journey.riskState)
        assertEquals(84, journey.batteryPercent)
    }

    @Test
    fun safeZoneModel_perimeter_isCorrect() {
        val zone = SafeZone(
            name = "Lincoln High School",
            type = "School",
            address = "Seattle, WA",
            radiusMeters = 300,
            expectedSchedule = "8:30 AM - 4:30 PM",
            isActive = true
        )
        assertEquals("Lincoln High School", zone.name)
        assertEquals(300, zone.radiusMeters)
        assertTrue(zone.isActive)
    }
}
