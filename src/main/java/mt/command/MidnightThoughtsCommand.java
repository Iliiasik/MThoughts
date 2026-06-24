package mt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import mt.config.MidnightThoughtsConfig;
import mt.network.NetworkHandler;
import mt.network.packet.SyncConfigPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class MidnightThoughtsCommand {

    private MidnightThoughtsCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("midnightthoughts")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("reload")
                                .executes(MidnightThoughtsCommand::reload))
        );
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        MidnightThoughtsConfig.reload();

        MinecraftServer server = ctx.getSource().getServer();
        SyncConfigPacket packet = buildPacket(MidnightThoughtsConfig.getInstance());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            NetworkHandler.sendConfig(player, packet);
        }

        int count = server.getPlayerList().getPlayerCount();
        ctx.getSource().sendSuccess(
                () -> Component.literal("[Midnight Thoughts] Config reloaded and synced to " + count + " player(s)."),
                true);
        return 1;
    }

    private static SyncConfigPacket buildPacket(MidnightThoughtsConfig cfg) {
        return new SyncConfigPacket(
                cfg.getSleepOverlay().minSlideDisplayTimeMs,
                cfg.getSleepOverlay().maxSlideDisplayTimeMs,
                cfg.getSleepOverlay().fadeInDurationMs,
                cfg.getSleepOverlay().fadeOutDurationMs,
                cfg.getSleepOverlay().overlayOpacity,
                cfg.getSleepOverlay().textOpacity,
                cfg.getSleepOverlay().imageOpacity,
                cfg.getSleepOverlay().specialSlideChance,
                cfg.getSleepOverlay().enableOverlay,
                cfg.getSleepOverlay().enableImage,
                cfg.getSleepOverlay().enableDailySummaryScreen,
                cfg.getSleepOverlay().useFactsApi,
                cfg.getSleepOverlay().userContentReplaces,
                cfg.getSleepOverlay().hideChatWhenSleeping,
                cfg.getUi().theme,
                cfg.getUi().hideWellRestedHud,
                cfg.getUi().hideThemeSwitchButton
        );
    }
}