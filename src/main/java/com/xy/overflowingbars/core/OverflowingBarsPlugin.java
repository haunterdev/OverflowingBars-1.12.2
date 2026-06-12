package com.xy.overflowingbars.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
public class OverflowingBarsPlugin implements IFMLLoadingPlugin {

    public OverflowingBarsPlugin() {
        // MixinBootstrap.init() NOT called here — MixinBooter coremod already initializes Mixin.
        // Calling it a second time causes LinkageError on GlobalProperties$Keys.
        Mixins.addConfiguration("mixins.overflowingbars.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
