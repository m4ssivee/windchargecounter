package com.m4ssive.windchargecounter.editor.layout;

/** Where a popover goes: top-left corner, whether it opened above its anchor, and the caret's x. */
public record Placement(int x, int y, boolean above, int caretX) {}
