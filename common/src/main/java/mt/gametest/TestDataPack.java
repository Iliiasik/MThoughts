package mt.gametest;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

final class TestDataPack {

    private static final String FOLDER = "midnightthoughts_gametest";
    private static final String PACK_ID = "file/" + FOLDER;
    private static final String PACK_META = "{\"pack\": {\"pack_format\": 48, \"description\": \"Midnight Thoughts game test\"}}";

    private TestDataPack() {}

    static void install(MinecraftServer server, Map<String, String> blockTags) {
        Path root = folder(server);
        try {
            Files.createDirectories(root);
            Files.writeString(root.resolve("pack.mcmeta"), PACK_META, StandardCharsets.UTF_8);
            Path tags = root.resolve("data/midnightthoughts/tags/block");
            Files.createDirectories(tags);
            for (Map.Entry<String, String> tag : blockTags.entrySet()) {
                Files.writeString(tags.resolve(tag.getKey() + ".json"), tag.getValue(), StandardCharsets.UTF_8);
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        reload(server);
    }

    static void uninstall(MinecraftServer server) {
        Path root = folder(server);
        if (!Files.exists(root)) return;
        try (Stream<Path> walk = Files.walk(root)) {
            for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        reload(server);
    }

    static String tag(boolean replace, String... blocks) {
        return "{\"replace\": " + replace + ", \"values\": [\"" + String.join("\", \"", blocks) + "\"]}";
    }

    private static void reload(MinecraftServer server) {
        PackRepository packs = server.getPackRepository();
        packs.reload();
        List<String> selected = new ArrayList<>(packs.getSelectedIds());
        if (packs.isAvailable(PACK_ID) && !selected.contains(PACK_ID)) selected.add(PACK_ID);
        CompletableFuture<Void> reload = server.reloadResources(selected);
        server.managedBlock(reload::isDone);
        reload.join();
    }

    private static Path folder(MinecraftServer server) {
        return server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(FOLDER);
    }
}
