package com.xy.overflowingbars.client.handler;

import com.xy.overflowingbars.client.helper.ChatOffsetHelper;
import com.xy.overflowingbars.config.OverflowingBarsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
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

        if (type == RenderGameOverlayEvent.ElementType.HEALTH && OverflowingBarsConfig.health.allowLayers) {
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
        } else if (type == RenderGameOverlayEvent.ElementType.ARMOR && OverflowingBarsConfig.armor.allowLayers) {
            event.setCanceled(true);
            GlStateManager.enableBlend();
            BarOverlayRenderer.renderArmorLevelBar(res.getScaledWidth(), res.getScaledHeight(), mc,
                    GuiIngameForge.left_height, OverflowingBarsConfig.armor.allowCount, false);
            GlStateManager.disableBlend();
            mc.getTextureManager().bindTexture(BarOverlayRenderer.GUI_ICONS_LOCATION);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if (ChatOffsetHelper.armorRow(mc.player)) {
                GuiIngameForge.left_height += 10;
            }
        }
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
