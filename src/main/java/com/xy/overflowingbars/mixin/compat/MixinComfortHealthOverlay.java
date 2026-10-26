package com.xy.overflowingbars.mixin.compat;

import com.xy.overflowingbars.compat.HealthOverlayCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Farmer's Delight Legacy's Comfort sheen, drawn on the layered health row instead of where
 * vanilla's multi-row layout would put the bottom row. FD Legacy has already checked the effect
 * and its config by the time this method runs. See {@link HealthOverlayCompat}.
 */
@Pseudo
@Mixin(targets = "com.wdcftgg.farmersdelightlegacy.client.gui.ComfortHealthOverlay", remap = false)
public abstract class MixinComfortHealthOverlay {

    @Inject(method = "renderComfortOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private static void overflowingbars$layeredComfort(Minecraft mc, EntityPlayer player, ScaledResolution resolution,
                                                       CallbackInfo ci) {
        if (HealthOverlayCompat.renderComfort(mc)) {
            ci.cancel();
        }
    }
}
