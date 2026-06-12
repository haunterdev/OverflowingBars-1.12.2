package com.xy.overflowingbars;

import com.xy.overflowingbars.client.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = OverflowingBars.MODID, name = OverflowingBars.NAME, version = OverflowingBars.VERSION, clientSideOnly = true)
public class OverflowingBars {

    public static final String MODID = "overflowingbars";
    public static final String NAME = "Overflowing Bars";
    public static final String VERSION = "8.0.3";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "com.xy.overflowingbars.client.proxy.ClientProxy",
        serverSide = "com.xy.overflowingbars.client.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }
}
