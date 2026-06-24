package mt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import mt.config.MidnightThoughtsConfig;
import mt.network.NetworkHandler;
import mt.network.packet.SyncConfigPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public final class MidnightThoughtsCommand {

    private MidnightThoughtsCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("midnightthoughts")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(literal("reload")
                                .executes(MidnightThoughtsCommand::reload))
        );
    }

    private static int reload(CommandContext<ServerCommandSource> ctx) {
        MidnightThoughtsConfig.reload();

        MinecraftServer server = ctx.getSource().getServer();
        SyncConfigPacket packet = buildPacket(MidnightThoughtsConfig.getInstance());
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            NetworkHandler.sendConfig(player, packet);
        }

        int count = server.getPlayerManager().getCurrentPlayerCount();
        ctx.getSource().sendFeedback(
                () -> Text.literal("[Midnight Thoughts] Config reloaded and synced to " + count + " player(s)."),
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