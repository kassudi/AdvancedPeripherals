package de.srendi.advancedperipherals.client;

import java.lang.reflect.Method;

/**
 * Small reflection based bridge to Iris/Oculus so we don't need them at compile time.
 * Used to avoid drawing overlay objects into the shadow map of shader packs.
 */
public final class ShaderCompat {
    private static final String[] API_CLASSES = {
        "net.irisshaders.iris.api.v0.IrisApi",
        "net.coderbot.iris.api.v0.IrisApi"
    };

    private static boolean resolved = false;
    private static Object api = null;
    private static Method isShadowPass = null;

    private ShaderCompat() {}

    private static void resolve() {
        resolved = true;
        for (String className : API_CLASSES) {
            try {
                Class<?> clazz = Class.forName(className);
                Object instance = clazz.getMethod("getInstance").invoke(null);
                isShadowPass = clazz.getMethod("isRenderingShadowPass");
                api = instance;
                return;
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // not this one, try the next
            }
        }
    }

    public static boolean isRenderingShadowPass() {
        if (!resolved) {
            resolve();
        }
        if (api == null) {
            return false;
        }
        try {
            return (boolean) isShadowPass.invoke(api);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }
}
