package com.m4ssive.windchargecounter.util;

import net.minecraft.client.gui.DrawContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class GuiHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger("WindChargeCounter");

    private static Method getMatricesMethod = null;
    private static Method pushMethod = null;
    private static Method popMethod = null;
    private static Method translateXYMethod = null;
    private static Method scaleXYMethod = null;
    private static boolean initialized = false;

    private static void init(DrawContext context) {
        if (initialized) return;
        initialized = true;
        try {
            for (Method m : context.getClass().getMethods()) {
                if (m.getParameterCount() == 0
                        && (m.getName().equals("getMatrices") || m.getName().equals("method_51448") || m.getName().equals("e"))) {
                    getMatricesMethod = m;
                    break;
                }
            }
            if (getMatricesMethod == null) {
                for (Method m : context.getClass().getMethods()) {
                    if (m.getParameterCount() == 0 && m.getReturnType().getName().contains("Matrix")) {
                        getMatricesMethod = m;
                        break;
                    }
                }
            }
            if (getMatricesMethod != null) {
                Object matrices = getMatricesMethod.invoke(context);
                if (matrices == null) return;
                Class<?> matClass = matrices.getClass();
                for (Method m : matClass.getMethods()) {
                    String name = m.getName();
                    int pc = m.getParameterCount();
                    if (pushMethod == null && pc == 0 && (name.equals("pushMatrix") || name.equals("push"))) {
                        pushMethod = m;
                    }
                    if (popMethod == null && pc == 0 && (name.equals("popMatrix") || name.equals("pop"))) {
                        popMethod = m;
                    }
                    if (translateXYMethod == null && name.equals("translate") && pc == 2) {
                        Class<?>[] pt = m.getParameterTypes();
                        if (pt[0] == float.class && pt[1] == float.class) {
                            translateXYMethod = m;
                        }
                    }
                    if (scaleXYMethod == null && name.equals("scale") && pc == 2) {
                        Class<?>[] pt = m.getParameterTypes();
                        if (pt[0] == float.class && pt[1] == float.class) {
                            scaleXYMethod = m;
                        }
                    }
                }
                if (translateXYMethod == null) {
                    for (Method m : matClass.getMethods()) {
                        if (m.getName().equals("translate") && m.getParameterCount() == 3) {
                            Class<?>[] pt = m.getParameterTypes();
                            if (pt[0] == float.class) { translateXYMethod = m; break; }
                        }
                    }
                }
                if (scaleXYMethod == null) {
                    for (Method m : matClass.getMethods()) {
                        if (m.getName().equals("scale") && m.getParameterCount() == 3) {
                            Class<?>[] pt = m.getParameterTypes();
                            if (pt[0] == float.class) { scaleXYMethod = m; break; }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.error("[GuiHelper] init failed", e);
        }
    }

    private static Object getMatrices(DrawContext context) {
        if (getMatricesMethod == null) return null;
        try { return getMatricesMethod.invoke(context); } catch (Throwable e) { return null; }
    }

    public static void push(DrawContext context) {
        init(context);
        if (pushMethod == null) return;
        try { pushMethod.invoke(getMatrices(context)); } catch (Throwable e) {}
    }

    public static void pop(DrawContext context) {
        init(context);
        if (popMethod == null) return;
        try { popMethod.invoke(getMatrices(context)); } catch (Throwable e) {}
    }

    public static void translate(DrawContext context, double x, double y, double z) {
        init(context);
        if (translateXYMethod == null) return;
        try {
            Object mat = getMatrices(context);
            if (translateXYMethod.getParameterCount() == 2)
                translateXYMethod.invoke(mat, (float) x, (float) y);
            else
                translateXYMethod.invoke(mat, (float) x, (float) y, (float) z);
        } catch (Throwable e) {}
    }

    public static void scale(DrawContext context, float x, float y, float z) {
        init(context);
        if (scaleXYMethod == null) return;
        try {
            Object mat = getMatrices(context);
            if (scaleXYMethod.getParameterCount() == 2)
                scaleXYMethod.invoke(mat, x, y);
            else
                scaleXYMethod.invoke(mat, x, y, z);
        } catch (Throwable e) {}
    }
}
