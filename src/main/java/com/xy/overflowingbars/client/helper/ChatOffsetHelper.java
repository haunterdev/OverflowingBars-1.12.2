package com.xy.overflowingbars.client.helper;

import com.xy.overflowingbars.compat.PotionCoreCompat;
import com.xy.overflowingbars.config.OverflowingBarsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

public class ChatOffsetHelper {

    public static int getChatOffsetY() {
        int offset = 0;
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null) return 0;

        if (twoHealthRows(mc.player)) ++offset;
        if (armorRow(mc.player)) ++offset;
        if (toughnessRow(mc.player) && (OverflowingBarsConfig.toughness.leftSide || offset == 0)) ++offset;

        return offset * 10;
    }

    public static boolean twoHealthRows(EntityPlayer p) {
        if (p.getAbsorptionAmount() <= 0.0F) return false;
        // Decide the reserved height the same way HealthBarRenderer.renderHearts lays out hearts:
        // by rounded heart counts, NOT by raw (maxHealth + absorption). Rounding each bar up
        // independently means the rendered hearts can wrap to a second row while the raw sum is
        // still <= 20, so a raw "> 20" test under-reserves height and the armor bar overlaps the
        // absorption row (notably with reduced max health + near-full absorption).
        float maxHealth = Math.max(p.getMaxHealth(), MathHelper.ceil(p.getHealth()));
        int normalHearts = Math.min(10, MathHelper.ceil((double) maxHealth / 2.0));
        int absorptionHearts = Math.min(20 - normalHearts, MathHelper.ceil((double) p.getAbsorptionAmount() / 2.0));
        return normalHearts + absorptionHearts > 10;
    }

    public static boolean armorRow(EntityPlayer p) {
        if (p.getTotalArmorValue() > 0) return true;
        // When we own the armor element, a resistance/magic-shield overlay reserves the
        // same row even with no armor underneath (Potion Core compat).
        return OverflowingBarsConfig.armor.allowLayers && PotionCoreCompat.hasArmorOverlay(p);
    }

    public static boolean toughnessRow(EntityPlayer p) {
        return OverflowingBarsConfig.toughness.armorToughnessBar
                && MathHelper.floor(p.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue()) > 0;
    }
}
