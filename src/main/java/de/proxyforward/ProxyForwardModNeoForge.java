package de.proxyforward;

//? if neoforge {
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import static de.proxyforward.ProxyForwardModCommon.*;

@Mod("proxyforward")
public class ProxyForwardModNeoForge {
    public ProxyForwardModNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Hello NeoForge world!");

        //? if !release
        LOGGER.info("ProxyForward loaded on NeoForge");
    }
}
//?}
