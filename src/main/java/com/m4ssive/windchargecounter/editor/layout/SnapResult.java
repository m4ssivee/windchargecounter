package com.m4ssive.windchargecounter.editor.layout;

/** Snapped position plus the screen line it aligned to on each axis (null = none). */
public record SnapResult(int x, int y, Integer guideX, Integer guideY) {}
