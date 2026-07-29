package com.xy.overflowingbars.compat;

import com.xy.overflowingbars.OverflowingBars;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Loader;

/**
 * Soft Potion Core attribute reader. Resolves PotionCoreAttributes fields
 * reflectively so OverflowingBars never hard-depends on potioncore.jar.
 *
 * Cache note: the resolved IAttribute instances are singletons, they are
 * the same object the entire JVM lifetime and never require re-resolution.
 */
public final class PotionCoreCompat {

    private static final String MOD_ID = "potioncore";

    private static boolean resolved;
    private static boolean potionCoreLoaded;
    /** DAMAGE_RESISTANCE, or null if not resolvable. */
    private static IAttribute damageResistance;
    /** MAGIC_SHIELDING, or null if not resolvable. */
    private static IAttribute magicShielding;

    private PotionCoreCompat() {}

    /** True when potioncore is loaded AND both attributes are accessible. */
    public static boolean isLoaded() {
        if (!resolved) resolve();
        return potionCoreLoaded;
    }

    /**
     * Damage Resistance points (0–20 scale).
     * {@code (attrValue - 1.0) * 20.0}, matching PotionCore's own formula
     * in {@code renderOverlaysPre}.
     *
     * @return 0.0 when PC is absent, the attribute is missing, or the player
     *         has no resistance.
     */
    public static double getResistancePoints(EntityPlayer player) {
        if (!isLoaded() || player == null || damageResistance == null) return 0.0;
        double val = player.getEntityAttribute(damageResistance).getAttributeValue();
        return (val - 1.0) * 20.0;
    }

    /**
     * Magic Shielding value (0–20 range per PotionCore RangedAttribute).
     *
     * @return 0.0 when PC is absent, the attribute is missing, or the player
     *         has no shielding.
     */
    public static double getMagicShielding(EntityPlayer player) {
        if (!isLoaded() || player == null || magicShielding == null) return 0.0;
        return player.getEntityAttribute(magicShielding).getAttributeValue();
    }

    // -----------------------------------------------------------------
    // Reflection resolution (mirrors ScalingHealthCompat pattern)
    // -----------------------------------------------------------------

    private static void resolve() {
        resolved = true;
        potionCoreLoaded = Loader.isModLoaded(MOD_ID);
        if (!potionCoreLoaded) return;

        try {
            Class<?> attrClass = Class.forName("com.tmtravlr.potioncore.PotionCoreAttributes");
            damageResistance = (IAttribute) attrClass.getField("DAMAGE_RESISTANCE").get(null);
            magicShielding   = (IAttribute) attrClass.getField("MAGIC_SHIELDING").get(null);
            OverflowingBars.LOGGER.info(
                    "Potion Core detected; enabling Damage Resistance + Magic Shielding overlay compat.");
        } catch (Exception e) {
            OverflowingBars.LOGGER.warn(
                    "Potion Core detected but its attributes could not be read ({}); "
                            + "armor overlay compat disabled.",
                    e.toString());
            potionCoreLoaded = false;
        }
    }
}
