package net.proxyforward.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import net.proxyforward.proxy.BungeeProxy;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerHandshakePacketListenerImpl.class)
public abstract class ServerHandshakePacketListenerImplMixin {

    @Final
    @Shadow
    private Connection connection;

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V"
        ),
        method = "beginLogin"
    )
    public void proxyforward$beginLogin(@NonNull ClientIntentionPacket packet, boolean transfer, @NonNull CallbackInfo callbackInfo) {
        BungeeProxy.beginLogin(packet, connection);
    }
}
