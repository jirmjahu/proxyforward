package net.proxyforward.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientIntentionPacket.class)
public abstract class ClientIntentionPacketMixin {

    @Redirect(
        at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/network/FriendlyByteBuf;readUtf(I)Ljava/lang/String;"
        ),
        method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V"
    )
    private static @NonNull String proxyforward$read(@NonNull FriendlyByteBuf buf, int maxLength) {
        return buf.readUtf(Short.MAX_VALUE);
    }
}
