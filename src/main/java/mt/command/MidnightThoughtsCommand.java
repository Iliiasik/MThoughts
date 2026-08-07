package mt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mt.config.MidnightThoughtsConfig;
import mt.network.NetworkHandler;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.UserContentPacket;
import mt.server.AchievementLoader;
import mt.server.ComfortCalculator;
import mt.server.UserContentInitializer;
import mt.server.WellRestedEffect;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

import java.util.Collection;

public final class MidnightThoughtsCommand {

    private MidnightThoughtsCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("midnightthoughts")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("reload")
                                .executes(MidnightThoughtsCommand::reloadAll)
                                .then(Commands.literal("config").executes(MidnightThoughtsCommand::reloadConfig))
                                .then(Commands.literal("achievements").executes(MidnightThoughtsCommand::reloadAchievements))
                                .then(Commands.literal("content").executes(MidnightThoughtsCommand::reloadContent))
                                .then(Commands.literal("all").executes(MidnightThoughtsCommand::reloadAll)))
                        .then(Commands.literal("comfort")
                                .executes(MidnightThoughtsCommand::comfortDebug)
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(MidnightThoughtsCommand::comfortDebugTarget)))
                        .then(Commands.literal("wellrested")
                                .then(Commands.literal("grant")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 5))
                                                        .executes(MidnightThoughtsCommand::wellRestedGrant))))
                                .then(Commands.literal("clear")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(MidnightThoughtsCommand::wellRestedClear))))
        );
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> ctx) {
        doReloadConfig(ctx.getSource().getServer());
        reply(ctx, "Config reloaded and synced.");
        return 1;
    }

    private static int reloadAchievements(CommandContext<CommandSourceStack> ctx) {
        int count = doReloadAchievements(ctx.getSource().getServer());
        reply(ctx, "Achievements reloaded (" + count + ") and synced.");
        return 1;
    }

    private static int reloadContent(CommandContext<CommandSourceStack> ctx) {
        doReloadContent(ctx.getSource().getServer());
        reply(ctx, "User content reloaded and synced.");
        return 1;
    }

    private static int reloadAll(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        doReloadConfig(server);
        int count = doReloadAchievements(server);
        doReloadContent(server);
        reply(ctx, "Reloaded config, achievements (" + count + ") and content.");
        return 1;
    }

    private static int comfortDebug(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return reportComfort(ctx.getSource(), ctx.getSource().getPlayerOrException());
    }

    private static int comfortDebugTarget(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return reportComfort(ctx.getSource(), EntityArgument.getPlayer(ctx, "target"));
    }

    private static int reportComfort(CommandSourceStack src, ServerPlayer player) {
        ComfortCalculator.ComfortDebug d = ComfortCalculator.debug(player);

        src.sendSuccess(() -> Component.literal("── Comfort Report: " + player.getName().getString() + " ──")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);

        if (!d.enabled()) {
            src.sendSuccess(() -> Component.literal("Comfort system is disabled in config.").withStyle(ChatFormatting.RED), false);
            return 1;
        }

        src.sendSuccess(() -> Component.literal("Scan radius: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(d.scanRadius())).withStyle(ChatFormatting.WHITE)), false);

        src.sendSuccess(() -> Component.literal("Cozy surroundings").withStyle(ChatFormatting.AQUA), false);
        for (int i = 0; i <= 4; i++) {
            Component line = categoryLine(d, i);
            src.sendSuccess(() -> line, false);
        }

        src.sendSuccess(() -> Component.literal("Unsettling surroundings").withStyle(ChatFormatting.LIGHT_PURPLE), false);
        for (int i = 5; i <= 7; i++) {
            Component line = categoryLine(d, i);
            src.sendSuccess(() -> line, false);
        }

        ChatFormatting totalColor = d.total() > 0 ? ChatFormatting.GREEN : (d.total() < 0 ? ChatFormatting.RED : ChatFormatting.YELLOW);
        String totalStr = (d.total() > 0 ? "+" : "") + d.total();
        src.sendSuccess(() -> Component.literal("Total comfort: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(totalStr).withStyle(totalColor, ChatFormatting.BOLD)), false);

        src.sendSuccess(() -> statusLine("Nightmares", d.nightmare(), d.nightmareEnabled(), d.nightmareThreshold()), false);
        src.sendSuccess(() -> statusLine("Sleep blocked", d.sleepBlocked(), d.sleepBlockEnabled(), d.sleepBlockThreshold()), false);
        return 1;
    }

    private static Component categoryLine(ComfortCalculator.ComfortDebug d, int i) {
        boolean found = d.found()[i];
        int weight = d.weights()[i];
        MutableComponent icon = found
                ? Component.literal("● ").withStyle(ChatFormatting.GREEN)
                : Component.literal("○ ").withStyle(ChatFormatting.DARK_GRAY);
        MutableComponent label = Component.literal(displayName(ComfortCalculator.CATEGORY_NAMES[i]))
                .withStyle(found ? ChatFormatting.WHITE : ChatFormatting.GRAY);
        MutableComponent value;
        if (found) {
            ChatFormatting c = weight > 0 ? ChatFormatting.GREEN : (weight < 0 ? ChatFormatting.RED : ChatFormatting.GRAY);
            value = Component.literal("  " + (weight > 0 ? "+" : "") + weight).withStyle(c);
        } else {
            value = Component.literal("  —").withStyle(ChatFormatting.DARK_GRAY);
        }
        return Component.literal("  ").append(icon).append(label).append(value);
    }

    private static Component statusLine(String label, boolean active, boolean enabled, int threshold) {
        MutableComponent name = Component.literal(label + ": ").withStyle(ChatFormatting.GRAY);
        MutableComponent value = active
                ? Component.literal("YES").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                : Component.literal("no").withStyle(ChatFormatting.GREEN);
        MutableComponent meta = enabled
                ? Component.literal(" (triggers at ≤ " + threshold + ")").withStyle(ChatFormatting.DARK_GRAY)
                : Component.literal(" (disabled)").withStyle(ChatFormatting.DARK_GRAY);
        return name.append(value).append(meta);
    }

    private static String displayName(String id) {
        return switch (id) {
            case "lighting" -> "Lighting";
            case "carpet" -> "Soft flooring";
            case "furniture" -> "Furniture";
            case "decoration" -> "Decoration";
            case "structure" -> "Solid structure";
            case "macabre" -> "Macabre blocks";
            case "hostile" -> "Hostile blocks";
            case "dark" -> "Darkness";
            default -> id;
        };
    }

    private static int wellRestedGrant(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!MidnightThoughtsConfig.getInstance().getWellRested().enabled) {
            replyFailure(ctx, "Well-rested is disabled in the server config.");
            return 0;
        }

        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
        int level = IntegerArgumentType.getInteger(ctx, "level");
        for (ServerPlayer player : targets) {
            WellRestedEffect.applyToPlayer(player, level);
        }
        int n = targets.size();
        reply(ctx, "Granted well-rested level " + level + " to " + n + " player(s).");
        return n;
    }

    private static int wellRestedClear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
        for (ServerPlayer player : targets) {
            WellRestedEffect.clear(player);
        }
        int n = targets.size();
        reply(ctx, "Cleared well-rested from " + n + " player(s).");
        return n;
    }

    private static void doReloadConfig(MinecraftServer server) {
        MidnightThoughtsConfig.reload();
        ComfortCalculator.clearCache();
        SyncConfigPacket packet = SyncConfigPacket.of(MidnightThoughtsConfig.getInstance());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            NetworkHandler.sendConfig(player, packet);
        }
    }

    private static int doReloadAchievements(MinecraftServer server) {
        var list = AchievementLoader.reload();
        SyncAchievementsPacket packet = new SyncAchievementsPacket(list);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            NetworkHandler.sendAchievements(player, packet);
        }
        return list.size();
    }

    private static void doReloadContent(MinecraftServer server) {
        UserContentInitializer.writeDefaultFiles();
        UserContentInitializer.invalidateCache();
        UserContentPacket packet = UserContentInitializer.buildPacket();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            NetworkHandler.sendUserContent(player, packet);
        }
    }

    private static void reply(CommandContext<CommandSourceStack> ctx, String message) {
        ctx.getSource().sendSuccess(() -> Component.literal("[Midnight Thoughts] " + message), true);
    }

    private static void replyFailure(CommandContext<CommandSourceStack> ctx, String message) {
        ctx.getSource().sendFailure(Component.literal("[Midnight Thoughts] " + message));
    }

}
