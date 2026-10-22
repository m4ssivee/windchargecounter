package com.m4ssive.windchargecounter.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Guards the Cancel snapshot: copyFrom must copy every public setting, including future ones. */
class ModConfigCompletenessTest {
    @Test
    void copyFromCopiesEveryPublicField() throws Exception {
        ModConfig src = new ModConfig();
        for (Field f : ModConfig.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            Class<?> t = f.getType();
            if (t == int.class) f.setInt(src, f.getInt(src) + 7);
            else if (t == float.class) f.setFloat(src, f.getFloat(src) + 0.5f);
            else if (t == boolean.class) f.setBoolean(src, !f.getBoolean(src));
            else if (t == String.class) f.set(src, "changed-" + f.getName());
            else throw new AssertionError("unhandled field type " + t + " for " + f.getName());
        }
        ModConfig dst = new ModConfig();
        dst.copyFrom(src);
        for (Field f : ModConfig.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            assertEquals(f.get(src), f.get(dst), "copyFrom misses " + f.getName());
        }
    }
}
