package mt.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MockPlayer {

    private final String name;
    private final UUID uuid;
    private final GameType gameType;
    private ServerPlayer player;
    private Connection connection;
    private EmbeddedChannel channel;
    private final List<Component> actionBar = new ArrayList<>();

    private MockPlayer(String name, UUID uuid, GameType gameType) {
        this.name = name;
        this.uuid = uuid;
        this.gameType = gameType;
    }

    static MockPlayer create(String name, GameType gameType) {
        return new MockPlayer(name, UUID.nameUUIDFromBytes(("midnightthoughts-test:" + name + ":" + System.nanoTime())
                .getBytes(java.nio.charset.StandardCharsets.UTF_8)), gameType);
    }

    MockPlayer sameProfile() {
        return new MockPlayer(name, uuid, gameType);
    }

    void connect(MinecraftServer server, ServerLevel level, BlockPos standAt) {
        ServerPlayer created = new ServerPlayer(server, level, new GameProfile(uuid, name));
        connection = new Connection(PacketFlow.SERVERBOUND);
        channel = new EmbeddedChannel(connection);
        server.getPlayerList().placeNewPlayer(connection, created);
        player = created;
        player.setGameMode(gameType);
        moveTo(level, standAt);
    }

    public void moveTo(ServerLevel level, BlockPos standAt) {
        player.teleportTo(level, standAt.getX() + 0.5, standAt.getY(), standAt.getZ() + 0.5, 0.0F, 0.0F);
    }

    public void leave() {
        if (isOnline()) {
            connection.disconnect(Component.literal("Leaving the game test"));
            connection.handleDisconnection();
        }
    }

    public boolean isOnline() {
        return player != null && !player.hasDisconnected();
    }

    void tick() {
        collectActionBar();
        if (isOnline() && player.isAlive()) {
            player.doTick();
        }
    }

    private void collectActionBar() {
        if (channel == null) return;
        Object message;
        while ((message = channel.readOutbound()) != null) {
            if (message instanceof ClientboundSystemChatPacket chat && chat.overlay()) {
                actionBar.add(chat.content());
            }
        }
    }

    public boolean sawActionBar(String translationKeyPrefix) {
        collectActionBar();
        for (Component component : actionBar) {
            if (component.getContents() instanceof TranslatableContents contents
                    && contents.getKey().startsWith(translationKeyPrefix)) {
                return true;
            }
        }
        return false;
    }

    public void forgetActionBar() {
        collectActionBar();
        actionBar.clear();
    }

    public ServerPlayer get() {
        return player;
    }

    public String name() {
        return name;
    }

    public UUID uuid() {
        return uuid;
    }

}
