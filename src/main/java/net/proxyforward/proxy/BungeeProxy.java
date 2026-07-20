package net.proxyforward.proxy;

import com.google.common.collect.HashMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.proxyforward.ProxyForward;
import net.proxyforward.mixin.accessors.ClientIntentionPacketAccessor;
import net.proxyforward.mixin.accessors.ConnectionAccessor;
import net.proxyforward.utils.JacksonUtils;
import org.jspecify.annotations.NonNull;

import tools.jackson.databind.JsonNode;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class BungeeProxy {

    private static final String NO_FORWARD_INFO = "If you wish to use IP forwarding, please enable it in your BungeeCord config as well!";

    private static final String PROPERTY_PARSE_ERROR = "Unable to parse forwarded profile properties.";

    private BungeeProxy() {
    }

    public static void beginLogin(@NonNull ClientIntentionPacket packet, @NonNull Connection connection) {
        if (!ProxyForward.instance().config().bungee()) {
            return;
        }

        if (packet.intention() == ClientIntent.STATUS) {
            return;
        }

        final String[] parts = packet.hostName().split("\00");

        if (parts.length != 3 && parts.length != 4) {
            disconnect(connection, NO_FORWARD_INFO);
            return;
        }

        final String host = parts[0];
        final String ip = parts[1];

        UUID uuid = null;
        try {
            uuid = parseUuid(parts[2]);
        } catch (Exception e) {
            disconnect(connection, NO_FORWARD_INFO);
        }

        final String profileProperties = parts.length == 4 ? parts[3] : null;

        ((ConnectionAccessor) connection).setAddress(new InetSocketAddress(ip, readPort(connection.getRemoteAddress())));

        ((ClientIntentionPacketAccessor) (Object) packet).setHostName(host);

        final Channel channel = ((ConnectionAccessor) connection).getChannel();

        final Data data = new Data();
        channel.attr(Data.KEY).set(data);
        data.uniqueId = uuid;

        if (profileProperties != null) {
            parseProperties(data, profileProperties, connection);
        }
    }

    public static GameProfile applyForwardedData(@NonNull Connection connection, @NonNull String currentName) {
        if (!ProxyForward.instance().config().bungee()) {
            return null;
        }
        final Channel channel = ((ConnectionAccessor) connection).getChannel();
        final Data data = channel.attr(Data.KEY).getAndSet(null);
        return data != null ? data.toGameProfile(currentName) : null;
    }

    private static void parseProperties(Data data, String profilePropertiesJson, Connection connection) {
        try {
            final JsonNode node = JacksonUtils.JSON_MAPPER.readTree(profilePropertiesJson);

            if (node.isArray()) {
                for (JsonNode element : node) {

                    final String name = element.has("name") ? element.get("name").asString() : null;
                    final String value = element.has("value") ? element.get("value").asString() : null;
                    final JsonNode signatureNode = element.get("signature");

                    final String signature = (signatureNode != null && !signatureNode.isNull())
                            ? signatureNode.asString()
                            : null;

                    if (name == null || value == null) {
                        continue;
                    }

                    data.properties.add(new Property(name, value, signature));
                }
            }
        } catch (Exception e) {
            disconnect(connection, PROPERTY_PARSE_ERROR);
        }
    }

    private static void disconnect(Connection connection, String message) {
        final Component disconnectReason = Component.literal(message).withStyle(ChatFormatting.RED);

        connection.send(new ClientboundLoginDisconnectPacket(disconnectReason));
        connection.disconnect(disconnectReason);
    }

    private static int readPort(SocketAddress address) {
        return address instanceof InetSocketAddress socket ? socket.getPort() : 0;
    }

    private static UUID parseUuid(String uuid) {
        return new UUID(Long.parseUnsignedLong(uuid.substring(0, 16),16), Long.parseUnsignedLong(uuid.substring(16),16));
    }

    private static final class Data {

        public static final AttributeKey<Data> KEY = AttributeKey.newInstance("proxyforward$bungee_data");

        private final List<Property> properties = new ArrayList<>();

        private UUID uniqueId;

        public @NonNull GameProfile toGameProfile(@NonNull String name) {
            final HashMultimap<String, Property> map = HashMultimap.create();
            for (final Property property : properties) {
                map.put(property.name(), property);
            }
            return new GameProfile(uniqueId, name, new PropertyMap(map));
        }
    }
}
