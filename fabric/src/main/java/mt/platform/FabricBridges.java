package mt.platform;

import mt.api.event.ComfortCalculatedCallback;
import mt.api.event.MvpDeterminedCallback;
import mt.api.event.NightmareCallback;
import mt.api.event.WellRestedAppliedCallback;
import mt.api.event.WellRestedExpiredCallback;
import mt.fabric.PersistentDataHolder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class FabricBridges {

    private FabricBridges() {}

    public static void install() {
        MTEvents.setBridge(new MTEvents.Bridge() {
            @Override
            public int comfortCalculated(ServerPlayer player, int level) {
                return ComfortCalculatedCallback.EVENT.invoker().onComfortCalculated(player, level);
            }

            @Override
            public void nightmare(ServerPlayer player, int comfortLevel) {
                NightmareCallback.EVENT.invoker().onNightmare(player, comfortLevel);
            }

            @Override
            public void wellRestedApplied(ServerPlayer player, int level, boolean mvp, int durationTicks) {
                WellRestedAppliedCallback.EVENT.invoker().onWellRestedApplied(player, level, mvp, durationTicks);
            }

            @Override
            public void wellRestedExpired(ServerPlayer player) {
                WellRestedExpiredCallback.EVENT.invoker().onWellRestedExpired(player);
            }

            @Override
            public void mvpDetermined(ServerPlayer mvp) {
                MvpDeterminedCallback.EVENT.invoker().onMvpDetermined(mvp);
            }
        });

        MTNetwork.setBridge(new MTNetwork.Bridge() {
            @Override
            public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
                ServerPlayNetworking.send(player, payload);
            }

            @Override
            public void sendToAll(MinecraftServer server, CustomPacketPayload payload) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    ServerPlayNetworking.send(player, payload);
                }
            }
        });

        MTPlatform.setBridge(new MTPlatform.Bridge() {
            @Override
            public boolean isModLoaded(String modId) {
                return FabricLoader.getInstance().isModLoaded(modId);
            }

            @Override
            public CompoundTag persistentData(Player player) {
                return ((PersistentDataHolder) player).midnightthoughts$getPersistentData();
            }
        });
    }
}
