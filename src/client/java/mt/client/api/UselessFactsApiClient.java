package mt.client.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UselessFactsApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final String API_BASE_URL = "https://uselessfacts.jsph.pl/api/v2/facts/random?language=";
    private static final int TIMEOUT_SECONDS = 5;
    private static final Gson GSON = new GsonBuilder().create();

    private final HttpClient httpClient;
    private final ExecutorService executor;

    public UselessFactsApiClient() {
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "MidnightThoughts-API");
            thread.setDaemon(true);
            return thread;
        });
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .executor(executor)
                .build();
    }

    public CompletableFuture<Optional<String>> fetchRandomFact(String languageCode) {
        String apiLanguage = mapLanguageCode(languageCode);
        String url = API_BASE_URL + apiLanguage;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return parseResponse(response.body());
                    }
                    LOGGER.warn("API returned status code: {}", response.statusCode());
                    return Optional.<String>empty();
                })
                .exceptionally(e -> {
                    LOGGER.debug("Failed to fetch fact from API: {}", e.getMessage());
                    return Optional.empty();
                });
    }

    private String mapLanguageCode(String languageCode) {
        if (languageCode.startsWith("de")) {
            return "de";
        }
        return "en";
    }

    private Optional<String> parseResponse(String json) {
        try {
            ApiResponse response = GSON.fromJson(json, ApiResponse.class);
            if (response != null && response.text != null && !response.text.isBlank()) {
                return Optional.of(response.text);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to parse API response: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public void shutdown() {
        executor.shutdown();
    }

    private static class ApiResponse {
        @SerializedName("text")
        String text;
    }
}

