package mt.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class AtomicFiles {

    private static final int ATOMIC_MOVE_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 15L;

    private AtomicFiles() {}

    public static void writeString(Path file, String content) throws IOException {
        Path parent = file.getParent();
        if (parent != null) Files.createDirectories(parent);

        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.writeString(temp, content, StandardCharsets.UTF_8);
            replace(temp, file);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static void replace(Path temp, Path file) throws IOException {
        for (int attempt = 1; attempt <= ATOMIC_MOVE_ATTEMPTS; attempt++) {
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                return;
            } catch (AtomicMoveNotSupportedException e) {
                break;
            } catch (FileSystemException e) {
                sleep();
            }
        }
        Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void sleep() {
        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
