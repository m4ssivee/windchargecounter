package com.m4ssive.windchargecounter.ui;

import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

class HexInputTest {
    @Test
    void typingAfterFocusReplacesPrefilledValue() {
        HexInput in = new HexInput();
        in.focus(0x8BE9FF);
        assertEquals("8BE9FF", in.text());
        in.type("b8a6ff");
        assertEquals("B8A6FF", in.text());
        assertEquals(OptionalInt.of(0xB8A6FF), in.value());
    }

    @Test
    void backspaceOnSelectionClearsAll() {
        HexInput in = new HexInput();
        in.focus(0x8BE9FF);
        in.backspace();
        assertEquals("", in.text());
    }

    @Test
    void pasteAcceptsHashAndAnyCase() {
        HexInput in = new HexInput();
        in.focus(0x000000);
        in.paste("#b8a6ff");
        assertEquals("B8A6FF", in.text());
    }

    @Test
    void invalidCharactersAreIgnoredAndShortTextIsInvalid() {
        HexInput in = new HexInput();
        in.focus(0x000000);
        in.type("zzfff");
        assertEquals("FFF", in.text());
        assertTrue(in.value().isEmpty());
    }

    @Test
    void neverLongerThanSix() {
        HexInput in = new HexInput();
        in.focus(0x000000);
        in.type("1234567890");
        assertEquals("123456", in.text());
    }
}
