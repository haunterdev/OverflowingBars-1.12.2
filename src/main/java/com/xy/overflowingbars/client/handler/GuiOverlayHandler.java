package com.xy.overflowingbars.client.handler;

import com.xy.overflowingbars.client.helper.ChatOffsetHelper;
import com.xy.overflowingbars.compat.PotionCoreCompat;
import com.xy.overflowingbars.compat.ScalingHealthCompat;
import com.xy.overflowingbars.config.OverflowingBarsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class GuiOverlayHandler {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            HealthBarRenderer.INSTANCE.onStartTick(Minecraft.getMinecraft());
        }
    }

    @SubscribeEvent
    public void onPreRenderOverlay(RenderGameOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.hideGUI) return;

        ScaledResolution res = event.getResolution();
        RenderGameOverlayEvent.ElementType type = event.getType();

        if (type == RenderGameOverlayEvent.ElementType.HEALTH && OverflowingBarsConfig.health.allowLayers
                && !ScalingHealthCompat.shouldDeferHealthBar()) {
            event.setCanceled(true);
            GlStateManager.enableBlend();
            BarOverlayRenderer.renderHealthLevelBars(res.getScaledWidth(), res.getScaledHeight(), mc,
                    GuiIngameForge.left_height, OverflowingBarsConfig.health.allowCount);
            GlStateManager.disableBlend();
            // Vanilla renderFood/renderAir/renderMount reuse the texture bound by renderHealth
            // without rebinding; restore vanilla icons so they don't draw from our custom sheet.
            mc.getTextureManager().bindTexture(BarOverlayRenderer.GUI_ICONS_LOCATION);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GuiIngameForge.left_height += ChatOffsetHelper.twoHealthRows(mc.player) ? 20 : 10;
            firePostOverlay(event, RenderGameOverlayEvent.ElementType.HEALTH);
        } else if (type == RenderGameOverlayEvent.ElementType.ARMOR && OverflowingBarsConfig.armor.allowLayers) {
            event.setCanceled(true);
            // We now own the armor element; switch off Potion Core's overlapping overlay
            // (once) and draw its resistance/magic-shield overlays ourselves, aligned to
            // our bar. Only happens when allowLayers is on, otherwise Potion Core keeps
            // rendering against the vanilla armor bar as normal.
            PotionCoreCompat.disablePotionCoreArmorOverlay();
            int armorPosX = res.getScaledWidth() / 2 - 91;
            int armorPosY = res.getScaledHeight() - GuiIngameForge.left_height;
            boolean armorRow = ChatOffsetHelper.armorRow(mc.player);
            GlStateManager.enableBlend();
            BarOverlayRenderer.renderArmorLevelBar(res.getScaledWidth(), res.getScaledHeight(), mc,
                    GuiIngameForge.left_height, OverflowingBarsConfig.armor.allowCount, false);
            boolean potionCoreRow = PotionCoreArmorRenderer.render(armorPosX, armorPosY, mc, mc.player, !armorRow);
            GlStateManager.disableBlend();
            mc.getTextureManager().bindTexture(BarOverlayRenderer.GUI_ICONS_LOCATION);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if (armorRow || potionCoreRow) {
                GuiIngameForge.left_height += 10;
            }
            firePostOverlay(event, RenderGameOverlayEvent.ElementType.ARMOR);
        } else if (type == RenderGameOverlayEvent.ElementType.HEALTHMOUNT && OverflowingBarsConfig.mount.allowLayers) {
            // HEALTHMOUNT only fires when the player is riding a living mount, so a
            // mount is guaranteed here. Render one compact row and reserve its height.
            event.setCanceled(true);
            GlStateManager.enableBlend();
            MountBarRenderer.renderMountHealth(res.getScaledWidth(), res.getScaledHeight(), mc,
                    GuiIngameForge.right_height, OverflowingBarsConfig.mount.allowCount);
            GlStateManager.disableBlend();
            // Air bubbles reuse the texture bound here without rebinding; restore vanilla icons.
            mc.getTextureManager().bindTexture(BarOverlayRenderer.GUI_ICONS_LOCATION);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GuiIngameForge.right_height += 10;
            firePostOverlay(event, RenderGameOverlayEvent.ElementType.HEALTHMOUNT);
        }
    }

    /**
     * Re-fire the matching {@code Post} overlay event after we cancel its {@code Pre}.
     *
     * Forge's {@code GuiIngameForge} renders each element as
     * {@code if (pre(TYPE)) return; ...; post(TYPE);}, so cancelling the Pre also
     * suppresses the Post. Other mods that draw on the Post of HEALTH/ARMOR/HEALTHMOUNT
     * (e.g. Quantum Things' lava charm "Flamer Bar" on ARMOR) would silently vanish.
     * Posting it ourselves, after we've drawn our bar and advanced the height counter,
     * restores the vanilla event contract so those overlays keep rendering and stacking.
     */
    private static void firePostOverlay(RenderGameOverlayEvent.Pre pre, RenderGameOverlayEvent.ElementType type) {
        MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(pre, type));
    }

    @SubscribeEvent
    public void onPostRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        OverflowingBarsConfig.ToughnessRowConfig cfg = OverflowingBarsConfig.toughness;
        if (!cfg.armorToughnessBar) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.hideGUI || mc.player == null) return;
        // Match vanilla: survival HUD elements only (hidden in creative/spectator).
        if (!mc.playerController.shouldDrawHUD() || !(mc.getRenderViewEntity() instanceof EntityPlayer)) return;

        ScaledResolution res = event.getResolution();
        GlStateManager.enableBlend();
        int height = cfg.leftSide ? GuiIngameForge.left_height : GuiIngameForge.right_height;
        BarOverlayRenderer.renderToughnessLevelBar(res.getScaledWidth(), res.getScaledHeight(), mc,
                height, cfg.allowCount, cfg.leftSide, !cfg.allowLayers);
        GlStateManager.disableBlend();
    }
}
