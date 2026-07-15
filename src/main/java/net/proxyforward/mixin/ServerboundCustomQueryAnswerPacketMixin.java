package net.proxyforward.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;
import net.proxyforward.ProxyForward;
import net.proxyforward.config.Config;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerboundCustomQueryAnswerPacket.class)
public final class ServerboundCustomQueryAnswerPacketMixin {

    @Shadow
    @Final
    private static int MAX_PAYLOAD_SIZE;

    @Inject(method = "readPayload", at = @At("HEAD"), cancellable = true)
    private static void proxyforward$readPayload(int transactionId, FriendlyByteBuf input, CallbackInfoReturnable<CustomQueryAnswerPayload> cir) {
        final Config config = ProxyForward.instance().config();

        if (!config.velocity()|| config.velocitySecret() == null || config.velocitySecret().isEmpty()) {
            return;
        }

        final FriendlyByteBuf payload = input.readNullable(reader -> {
            final int size = reader.readableBytes();

            if (size > MAX_PAYLOAD_SIZE) {
                throw new IllegalArgumentException("Payload may not be larger than " + MAX_PAYLOAD_SIZE + " bytes");
            }

            return new FriendlyByteBuf(reader.readBytes(size));
        });

        cir.setReturnValue(payload == null ? null : (CustomQueryAnswerPayload) buffer -> buffer.writeBytes(payload.copy()));
    }
}
