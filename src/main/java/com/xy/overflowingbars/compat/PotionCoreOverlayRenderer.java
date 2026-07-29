package com.xy.overflowingbars.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

/**
 * Replicates Potion Core's Damage Resistance (red armor-shape overlay)
 * and Magic Shielding (enchantment glint bar) rendering.
 *
 * These draw on top of the armor bar itself (same Y row) at the correct Z
 * depth so they layer naturally with the armour icons.
 *
 * Textures are loaded from {@code potioncore:} domain at runtime, they
 * only exist when Potion Core is installed, so every public entry point
 * guards on {@link PotionCoreCompat#isLoaded()}.
 */
public final class PotionCoreOverlayRenderer {

    // Potion Core texture paths, resolved at runtime via the mod's domain
    private static final ResourceLocation ARMOR_OVERLAYS =
            new ResourceLocation("potioncore", "textures/gui/armor_overlays.png");
    private static final ResourceLocation ARMOR_MAGIC_SHIELD =
            new ResourceLocation("potioncore", "textures/gui/armor_magic_shield.png");

    private PotionCoreOverlayRenderer() {}

    // =================================================================
    // Damage Resistance, red armour-shaped outline at each armour slot
    // =================================================================

    /**
     * Draw the red Damage Resistance outline <b>before</b> the armour bar so
     * the armour icons cover its centre, leaving a 1-px red border visible
     * (the "outline" effect).
     *
     * @param posX  left edge of the armour row ({@code width/2 - 91})
     * @param posY  top edge of the armour row (screen height - leftHeight)
     * @param mc    Minecraft instance
     */
    public static void drawResistanceOverlay(int posX, int posY, Minecraft mc) {
        EntityPlayer player = mc.player;
        if (player == null) return;

        double resistance = PotionCoreCompat.getResistancePoints(player);
        if (resistance <= 0.0) return;

        GlStateManager.enableBlend();
        double z = -100.0;                    // behind magic shield, in front of armour
        int currentLeft = posX;

        mc.getTextureManager().bindTexture(ARMOR_OVERLAYS);

        // Walk 10 armour slots (i = 2, 4, 6, ..., 20), same as PC Pre
        for (int i = 2; resistance > 0.0 && i <= 20; i += 2) {
            if ((double) i <= resistance) {
                // Full frame (27, 0) size 11×11
                drawArmorOverlayRect(currentLeft - 1, posY - 1, z - 10.0,
                        27.0, 0.0, 11.0, 11.0);
            } else if ((double) i > resistance && resistance >= (double) (i - 2)) {
                // Partial frame, resistance falls in the middle of this slot
                int partial = MathHelper.floor(resistance % 2.0 * 11.0 / 2.0);
                drawArmorOverlayRect(currentLeft - 1, posY - 1, z - 10.0,
                        27.0, 0.0, partial, 11.0);
            }
            currentLeft += 8;
        }
    }

    // =================================================================
    // Magic Shielding, animated enchantment-style glint bar
    // =================================================================

    /**
     * Draw the Magic Shielding glint bar above the armour icons.  The glint
     * width grows with the shielding level (max 83 px = 10 full slots).
     *
     * @param posX  left edge of the armour row
     * @param posY  top edge of the armour row
     * @param mc    Minecraft instance
     */
    public static void drawMagicShieldGlint(int posX, int posY, Minecraft mc) {
        EntityPlayer player = mc.player;
        if (player == null) return;

        double magicShield = PotionCoreCompat.getMagicShielding(player);
        if (magicShield <= 0.0) return;

        GlStateManager.enableBlend();

        mc.getTextureManager().bindTexture(ARMOR_MAGIC_SHIELD);

        // glintWidth = min(82, floor(magicShield * 4 + 2)); 83 at max bar
        int glintWidth = MathHelper.floor(magicShield * 4.0 + 2.0);
        glintWidth = Math.min(82, glintWidth);
        if (glintWidth == 82) glintWidth = 83;

        if (glintWidth > 2) {
            // Draw at z=0 so the GL_EQUAL depth test matches the armour icons.
            drawEnchantmentGlint(posX - 1, posY - 1, 0.0, glintWidth, 11.0);
        }
    }

    // =================================================================
    // Internal: tessellator rectangles for overlays
    // =================================================================

    /**
     * Draw a rectangle from the ARMOR_OVERLAYS texture.
     * Texture is 64×32 → uMult=1/64, vMult=1/32 (0.015625 / 0.03125).
     */
    private static void drawArmorOverlayRect(double x, double y, double z,
                                             double uStart, double vStart,
                                             double width, double height) {
        double uEnd = uStart + width;
        double vEnd = vStart + height;
        double uMult = 0.015625;    // 1/64
        double vMult = 0.03125;     // 1/32

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder vb = tess.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.pos(x, y + height, z).tex(uStart * uMult, vEnd * vMult).endVertex();
        vb.pos(x + width, y + height, z).tex(uEnd * uMult, vEnd * vMult).endVertex();
        vb.pos(x + width, y, z).tex(uEnd * uMult, vStart * vMult).endVertex();
        vb.pos(x, y, z).tex(uStart * uMult, vStart * vMult).endVertex();
        tess.draw();
    }

    // =================================================================
    // Enchantment glint (replicates PotionCore's drawEnchantmentGlint)
    // =================================================================

    /**
     * Renders the animated enchantment shimmer over a rectangular area.
     * Uses the standard GL_EQUAL depth test + texture-matrix rotation/scroll
     * so the shimmer only appears over existing armour-bar fragments.
     */
    private static void drawEnchantmentGlint(double x, double y, double z,
                                             double width, double height) {
        float scale = 8.0F;

        // Switch to GL_TEXTURE matrix for UV rotation
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.pushMatrix();

        // Standard enchantment-glint state: alpha test, depth equal (so the
        // glint only shows where armour icons are), no depth writes.
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR,
                GlStateManager.DestFactor.ONE);
        GlStateManager.depthMask(false);
        GlStateManager.depthFunc(GL11.GL_EQUAL);
        GlStateManager.color(0.95F, 0.75F, 0.95F);

        // First pass, scroll slowly, angle -50°
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        float scroll1 = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
        GlStateManager.translate(scroll1, 0.0F, 0.0F);
        GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
        drawGlintRect(x, y, z, width, height);
        GlStateManager.popMatrix();

        // Second pass, scroll faster, angle +10°
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        float scroll2 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
        GlStateManager.translate(-scroll2, 0.0F, 0.0F);
        GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
        drawGlintRect(x, y, z, width, height);
        GlStateManager.popMatrix();

        // Restore GL state
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.depthMask(true);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Draw one rectangle on the glint sheet at the given world position.
     * UV spans [0, uWidth] × [0, 1], uWidth is the fraction of the
     * (looping) glint texture occupied by this bar.
     */
    private static void drawGlintRect(double x, double y, double z,
                                      double width, double height) {
        double uWidth = width / 80.0;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder vb = tess.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.pos(x, y + height, z).tex(0.0, 1.0).endVertex();
        vb.pos(x + width, y + height, z).tex(uWidth, 1.0).endVertex();
        vb.pos(x + width, y, z).tex(uWidth, 0.0).endVertex();
        vb.pos(x, y, z).tex(0.0, 0.0).endVertex();
        tess.draw();
    }
}
