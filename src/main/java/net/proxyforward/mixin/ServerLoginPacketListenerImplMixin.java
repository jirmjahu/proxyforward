package net.proxyforward.mixin;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;
import net.minecraft.network.protocol.login.custom.CustomQueryPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.proxyforward.ProxyForward;
import net.proxyforward.config.Config;
import net.proxyforward.mixin.accessors.ConnectionAccessor;
import net.proxyforward.proxy.VelocityProxy;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private Connection connection;

    @Shadow
    private GameProfile authenticatedProfile;

    @Unique
    private int velocityLoginMessageId = -1;

    @Shadow
    public abstract void disconnect(Component component);

    @Shadow
    protected abstract void startClientVerification(GameProfile profile);

    @Inject(method = "handleHello", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;startClientVerification(Lcom/mojang/authlib/GameProfile;)V"), cancellable = true)
    private void proxyforward$handleHello(ServerboundHelloPacket packet, CallbackInfo ci) {
        final Config config = ProxyForward.instance().config();

        if (!config.velocity()|| config.velocitySecret() == null || config.velocitySecret().isEmpty()) {
            return;
        }

        velocityLoginMessageId = ThreadLocalRandom.current().nextInt();

        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeByte(VelocityProxy.MAX_SUPPORTED_FORWARDING_VERSION);

        final CustomQueryPayload payload = new CustomQueryPayload() {
            @Override
            public @NonNull Identifier id() {
                return VelocityProxy.PLAYER_INFO_CHANNEL;
            }

            @Override
            public void write(FriendlyByteBuf buffer) {
                buffer.writeBytes(buf);
            }
        };

        final ClientboundCustomQueryPacket queryPacket = new ClientboundCustomQueryPacket(velocityLoginMessageId, payload);

        connection.send(queryPacket);
        ci.cancel();
    }

    @Inject(method = "handleCustomQueryPacket", at = @At("HEAD"), cancellable = true)
    private void proxyforward$handleCustomQueryPacket(ServerboundCustomQueryAnswerPacket packet, CallbackInfo ci) {
        final Config config = ProxyForward.instance().config();

        if (!config.velocity() || config.velocitySecret() == null || config.velocitySecret().isEmpty() || packet.transactionId() != velocityLoginMessageId) {
            return;
        }

        ci.cancel();

        final CustomQueryAnswerPayload payload = packet.payload();
        FriendlyByteBuf buf = null;

        try {
            if (payload == null || velocityLoginMessageId == -1) {
                disconnect(Component.literal("This server requires you to connect with Velocity."));
                return;
            }

            buf = new FriendlyByteBuf(Unpooled.buffer());
            payload.write(buf);
            if (!VelocityProxy.checkIntegrity(buf)) {
                this.disconnect(Component.literal("Unable to verify player details."));
                return;
            }

            final int version = buf.readVarInt();
            if (version > VelocityProxy.MAX_SUPPORTED_FORWARDING_VERSION) {
                throw new IllegalStateException("Unsupported forwarding version " + version + ", wanted upto " + VelocityProxy.MAX_SUPPORTED_FORWARDING_VERSION);
            }

            final SocketAddress remoteAddress = connection.getRemoteAddress();
            final int port = remoteAddress instanceof InetSocketAddress socket
                    ? socket.getPort()
                    : 0;

            ((ConnectionAccessor) connection).setAddress(
                    new InetSocketAddress(VelocityProxy.readAddress(buf), port)
            );

            authenticatedProfile = VelocityProxy.createProfile(buf);

            LOGGER.info("UUID of player {} is {}", authenticatedProfile.name(), authenticatedProfile.id());

            startClientVerification(authenticatedProfile);
        } catch (Exception ex) {
            disconnect(Component.literal("Failed to verify."));
            ProxyForward.LOGGER.warn("Failed to verify {}", authenticatedProfile.name(), ex);
        } finally {
            if (buf != null) {
                buf.release();
            }
        }
    }
}
