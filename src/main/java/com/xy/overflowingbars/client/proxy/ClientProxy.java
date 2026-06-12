package com.xy.overflowingbars.client.proxy;

import com.xy.overflowingbars.client.handler.GuiOverlayHandler;
import com.xy.overflowingbars.config.OverflowingBarsConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        OverflowingBarsConfig.init(event.getSuggestedConfigurationFile());
        MinecraftForge.EVENT_BUS.register(new GuiOverlayHandler());
        MinecraftForge.EVENT_BUS.register(new OverflowingBarsConfig());
    }
}
