package com.xy.overflowingbars.mixin.compat;

import com.xy.overflowingbars.compat.HealthOverlayCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Brewin' And Chewin' Legacy's Tipsy hearts, drawn on the layered health row so hearts past 20
 * health land on their layered column instead of an undrawn second row. See
 * {@link HealthOverlayCompat}.
 */
@Pseudo
@Mixin(targets = "xy177.brewinandchewinlegacy.client.event.BNCClientEffectEvents", remap = false)
public abstract class MixinBNCClientEffectEvents {

    @Inject(method = "renderTipsyHearts", at = @At("HEAD"), cancellable = true, require = 0)
    private static void overflowingbars$layeredTipsyHearts(Minecraft mc, ScaledResolution resolution, float tipsyDamage,
                                                           CallbackInfo ci) {
        if (HealthOverlayCompat.renderTipsyHearts(mc, tipsyDamage)) {
            ci.cancel();
        }
    }
}
