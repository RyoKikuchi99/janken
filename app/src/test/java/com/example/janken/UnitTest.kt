package com.example.janken

import org.junit.Test

class UnitTest {
    // Test the judge function in MainActivity.kt
    @Test
    fun testJudge() {
        // Test the judge function
        assert(judge(0, 0) == 2)
        assert(judge(0, 1) == 0)
        assert(judge(0, 2) == 1)
        assert(judge(1, 0) == 1)
        assert(judge(1, 1) == 2)
        assert(judge(1, 2) == 0)
        assert(judge(2, 0) == 0)
        assert(judge(2, 1) == 1)
        assert(judge(2, 2) == 2)
    }
}