package net.proxyforward.proxy;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.net.InetAddresses;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.proxyforward.ProxyForward;

public final class VelocityProxy {

    public static final int MODERN_LAZY_SESSION = 4;
    public static final byte MAX_SUPPORTED_FORWARDING_VERSION = MODERN_LAZY_SESSION;
    public static final Identifier PLAYER_INFO_CHANNEL = Identifier.fromNamespaceAndPath("velocity", "player_info");

    public static boolean checkIntegrity(FriendlyByteBuf buf) {
        final byte[] signature = new byte[32];
        buf.readBytes(signature);

        final byte[] data = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), data);

        try {
            final Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(new SecretKeySpec(ProxyForward.instance().config().velocitySecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));

            final byte[] mySignature = mac.doFinal(data);
            if (!MessageDigest.isEqual(signature, mySignature)) {
                return false;
            }

        } catch (final InvalidKeyException | NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
        return true;
    }

    public static InetAddress readAddress(FriendlyByteBuf buf) {
        return InetAddresses.forString(buf.readUtf(Short.MAX_VALUE));
    }

    public static GameProfile createProfile(FriendlyByteBuf buf) {
        return new GameProfile(buf.readUUID(), buf.readUtf(16), readProperties(buf));
    }

    private static PropertyMap readProperties(FriendlyByteBuf buf) {
        final ImmutableMultimap.Builder<String, Property> propertiesBuilder = ImmutableMultimap.builder();
        final int properties = buf.readVarInt();

        for (int i = 0; i < properties; i++) {
            final String name = buf.readUtf(Short.MAX_VALUE);
            final String value = buf.readUtf(Short.MAX_VALUE);
            final String signature = buf.readBoolean() ? buf.readUtf(Short.MAX_VALUE) : null;
            propertiesBuilder.put(name, new Property(name, value, signature));
        }

        final ImmutableMultimap<String, Property> propertiesMap = propertiesBuilder.build();
        return new PropertyMap(propertiesMap);
    }
}