package com.lab.opengarage.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CarTest {
    @Test
    fun modelKey_joins_make_and_model_with_underscore() {
        assertEquals("현대_프라이드", Car.makeModelKey(" 현대 ", " 프라이드 "))
    }
}
