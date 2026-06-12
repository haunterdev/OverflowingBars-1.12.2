package com.xy.overflowingbars.client.helper;

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
        return p.getAbsorptionAmount() > 0.0F && p.getMaxHealth() + p.getAbsorptionAmount() > 20.0F;
    }

    public static boolean armorRow(EntityPlayer p) {
        return p.getTotalArmorValue() > 0;
    }

    public static boolean toughnessRow(EntityPlayer p) {
        return OverflowingBarsConfig.toughness.armorToughnessBar
                && MathHelper.floor(p.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue()) > 0;
    }
}
