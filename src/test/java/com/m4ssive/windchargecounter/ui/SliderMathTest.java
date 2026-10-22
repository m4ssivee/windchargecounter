package com.m4ssive.windchargecounter.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SliderMathTest {
    @Test
    void leftEdgeIsMin() {
        assertEquals(0.5f, Slider.valueFromMouse(10, 10, 250, 0.5f, 3.0f, 0.1f), 1e-6);
    }

    @Test
    void pastRightEdgeClampsToMax() {
        assertEquals(3.0f, Slider.valueFromMouse(999, 10, 250, 0.5f, 3.0f, 0.1f), 1e-6);
    }

    @Test
    void middleRoundsToStep() {
        assertEquals(1.8f, Slider.valueFromMouse(140, 10, 250, 0.5f, 3.0f, 0.1f), 1e-6);
        assertEquals(16f, Slider.valueFromMouse(41, 10, 124, 0f, 64f, 1f), 1e-6);
    }
}
