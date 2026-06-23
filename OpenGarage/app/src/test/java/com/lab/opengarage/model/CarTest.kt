package com.lab.opengarage.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CarTest {
    @Test
    fun modelKey_joins_make_and_model_with_underscore() {
        assertEquals("현대_프라이드", Car.makeModelKey(" 현대 ", " 프라이드 "))
    }

    @Test
    fun modelKey_normalizes_model_case_space_and_hyphen() {
        val a = Car.makeModelKey("BMW", "3 Series")
        val b = Car.makeModelKey("BMW", "3-SERIES")
        val c = Car.makeModelKey("BMW", "3series")
        assertEquals(a, b)
        assertEquals(a, c)
        assertEquals("BMW_3series", a)
    }
}
