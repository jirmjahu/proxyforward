package de.proxyforward;

//? if fabric {
/*import net.fabricmc.api.ModInitializer;

import static de.proxyforward.ProxyForwardModCommon.*;

public class ProxyForwardModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");

        //? if !release
        LOGGER.info("ProxyForward loaded on Fabric");

        //? if fapi: <0.100
        LOGGER.info("Fabric API is old on this version");
    }
}
*///?}
