package net.proxyforward.mixin;

import net.minecraft.server.MinecraftServer;
import net.proxyforward.ProxyForward;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow
    public abstract boolean usesAuthentication();

    @Inject(at = @At("HEAD"), method = "runServer")
    private void proxyforward$runServer(CallbackInfo ci) {
        if (usesAuthentication()) {
            ProxyForward.LOGGER.warn("Online mode is enabled! Proxy forwarding requires online-mode=false in server.properties.");
        }
    }
}
