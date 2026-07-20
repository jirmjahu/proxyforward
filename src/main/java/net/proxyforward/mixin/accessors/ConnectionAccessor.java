package net.proxyforward.mixin.accessors;

import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import io.netty.channel.Channel;

import java.net.SocketAddress;

@Mixin(Connection.class)
public interface ConnectionAccessor {

    @Mutable
    @Accessor("address")
    void setAddress(SocketAddress address);

    @Accessor("channel")
    Channel getChannel();

}