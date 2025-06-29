package com.udacity.catpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


public class AppTest {
    @Test
    public void verifyBasicMathOperation() {
        int expectedResult = 4;
        int actualResult = 2 + 2;
        assertEquals(expectedResult, actualResult, "is that correct?");
    }
}