package com.xy.overflowingbars.compat;

import com.xy.overflowingbars.OverflowingBars;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Field;

/**
 * Soft compat with Scaling Health (1.12.2).
 *
 * Scaling Health's {@code HeartDisplayHandler} subscribes to
 * {@code RenderGameOverlayEvent.Pre} with {@code receiveCanceled=true}, so it
 * draws its custom hearts even after we cancel the HEALTH element. Running both
 * health renderers stacks two heart bars and has caused client crashes when
 * taking damage. When Scaling Health is installed and its custom heart
 * rendering is enabled, we yield the health bar entirely and let it take over;
 * our armor and toughness bars are unaffected.
 */
public final class ScalingHealthCompat {

    private static final String MOD_ID = "scalinghealth";
    private static final String CONFIG_CLASS = "net.silentchaos512.scalinghealth.config.Config$Client$Hearts";
    private static final String CUSTOM_RENDERING_FIELD = "customHeartRendering";

    private static boolean resolved;
    private static boolean scalingHealthLoaded;
    /** Null when Scaling Health is absent or the field could not be found. */
    private static Field customHeartRendering;

    private ScalingHealthCompat() {}

    /**
     * True when Scaling Health is installed and currently rendering its own
     * heart bar. Reads the live config field so Scaling Health's in-game
     * config reload is respected without a restart.
     */
    public static boolean shouldDeferHealthBar() {
        if (!resolved) {
            resolve();
        }
        if (!scalingHealthLoaded) {
            return false;
        }
        if (customHeartRendering == null) {
            // Scaling Health present but config layout unknown (future version?).
            // Its handler ignores our cancellation either way, so yielding is
            // the only mode that cannot double-render.
            return true;
        }
        try {
            return customHeartRendering.getBoolean(null);
        } catch (IllegalAccessException e) {
            return true;
        }
    }

    private static void resolve() {
        resolved = true;
        scalingHealthLoaded = Loader.isModLoaded(MOD_ID);
        if (!scalingHealthLoaded) {
            return;
        }
        try {
            Field field = Class.forName(CONFIG_CLASS).getDeclaredField(CUSTOM_RENDERING_FIELD);
            field.setAccessible(true);
            if (field.getType() == boolean.class) {
                customHeartRendering = field;
            }
            OverflowingBars.LOGGER.info(
                    "Scaling Health detected; health bar will defer to its custom heart rendering when enabled.");
        } catch (ReflectiveOperationException e) {
            OverflowingBars.LOGGER.warn(
                    "Scaling Health detected but its config could not be read ({}); "
                            + "health bar disabled while it is installed to avoid double rendering.",
                    e.toString());
        }
    }
}
