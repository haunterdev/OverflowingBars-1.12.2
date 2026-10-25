package com.xy.overflowingbars.compat;

import com.xy.overflowingbars.OverflowingBars;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Field;

/**
 * Soft compat with Potion Core (1.12.2).
 *
 * Potion Core's {@code PotionCoreEventHandlerClient} draws extra things on the
 * vanilla armor bar: a damage-resistance outline, a magic-shielding glint, armor
 * values over 20, and armor toughness. We already render armor, toughness, and the
 * over-20 overflow ourselves, and we cancel the vanilla {@code ARMOR} element to do
 * it. That leaves Potion Core drawing its overlays at vanilla coordinates keyed off
 * {@code GuiIngameForge.left_height}, which no longer matches our compacted layout,
 * so its overflow/toughness icons double up and its resistance/magic-shield overlays
 * land in the wrong spot.
 *
 * Potion Core exposes a single global toggle for all of this
 * ({@code ConfigLoader.renderArmorOverlay}) and no per-feature switch, so its armor overlay
 * is switched off once at runtime, and the two things we don't already draw (damage
 * resistance and magic shielding) are drawn aligned to our own armor bar (see
 * {@code PotionCoreArmorRenderer}).
 */
public final class PotionCoreCompat {

    private static final String MOD_ID = "potioncore";
    private static final String ATTRIBUTES_CLASS = "com.tmtravlr.potioncore.PotionCoreAttributes";
    private static final String CONFIG_CLASS = "com.tmtravlr.potioncore.ConfigLoader";

    private static boolean resolved;
    private static boolean loaded;
    /** Null when Potion Core is absent or the fields could not be found. */
    private static IAttribute damageResistance;
    private static IAttribute magicShielding;
    private static Field renderArmorOverlay;
    /** Latches once we have switched Potion Core's own armor overlay off. */
    private static boolean overlayDisabled;

    private PotionCoreCompat() {}

    /** True when Potion Core is installed and its attributes resolved. */
    public static boolean isActive() {
        if (!resolved) {
            resolve();
        }
        return loaded && damageResistance != null && magicShielding != null;
    }

    /**
     * Damage-resistance display points (0-20), matching Potion Core's own formula:
     * {@code (DAMAGE_RESISTANCE - 1.0) * 20}. Zero when inactive.
     */
    public static double getResistancePoints(EntityPlayer player) {
        if (!isActive() || player == null) return 0.0;
        return (player.getEntityAttribute(damageResistance).getAttributeValue() - 1.0) * 20.0;
    }

    /** Raw magic-shielding attribute value (0-20). Zero when inactive. */
    public static double getMagicShielding(EntityPlayer player) {
        if (!isActive() || player == null) return 0.0;
        return player.getEntityAttribute(magicShielding).getAttributeValue();
    }

    /** True when this player has any resistance or magic-shield value worth drawing. */
    public static boolean hasArmorOverlay(EntityPlayer player) {
        return getResistancePoints(player) > 0.0 || getMagicShielding(player) > 0.0;
    }

    /**
     * Switch Potion Core's own armor overlay off so it stops double-drawing over our
     * compacted bar. Idempotent and lazy: called on first render, by which point Potion
     * Core has long since loaded its config. No-op when Potion Core is absent.
     */
    public static void disablePotionCoreArmorOverlay() {
        if (overlayDisabled || !isActive() || renderArmorOverlay == null) return;
        overlayDisabled = true;
        try {
            renderArmorOverlay.setBoolean(null, false);
            OverflowingBars.LOGGER.info(
                    "Potion Core detected; took over its armor bar overlays (resistance, magic shielding) "
                            + "and disabled Potion Core's own rendering to avoid double-drawing.");
        } catch (IllegalAccessException e) {
            OverflowingBars.LOGGER.warn(
                    "Potion Core detected but its armor overlay toggle could not be disabled ({}); "
                            + "its overlays may double-render over the armor bar.", e.toString());
        }
    }

    private static void resolve() {
        resolved = true;
        loaded = Loader.isModLoaded(MOD_ID);
        if (!loaded) {
            return;
        }
        try {
            Class<?> attrs = Class.forName(ATTRIBUTES_CLASS);
            damageResistance = (IAttribute) attrs.getField("DAMAGE_RESISTANCE").get(null);
            magicShielding = (IAttribute) attrs.getField("MAGIC_SHIELDING").get(null);
            renderArmorOverlay = Class.forName(CONFIG_CLASS).getField("renderArmorOverlay");
        } catch (ReflectiveOperationException | ClassCastException e) {
            damageResistance = null;
            magicShielding = null;
            renderArmorOverlay = null;
            OverflowingBars.LOGGER.warn(
                    "Potion Core detected but its armor attributes could not be read ({}); "
                            + "resistance/magic-shield overlays will not render.", e.toString());
        }
    }
}
