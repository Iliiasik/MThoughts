package mt.client.api;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UselessFactsApiClientTest {

    private HttpServer server;
    private final List<String> queries = new ArrayList<>();
    private int status = 200;
    private String body = "";

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/facts", exchange -> {
            queries.add(exchange.getRequestURI().getQuery());
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private UselessFactsApiClient client() {
        return new UselessFactsApiClient("http://127.0.0.1:" + server.getAddress().getPort() + "/facts?language=");
    }

    private Optional<String> fetch(String language) throws Exception {
        return client().fetchRandomFact(language).get(10, TimeUnit.SECONDS);
    }

    @Test
    void theFactTextIsReturned() throws Exception {
        body = "{\"id\":\"1\",\"text\":\"Bananas are berries\",\"language\":\"en\"}";

        assertEquals(Optional.of("Bananas are berries"), fetch("en_us"));
    }

    @Test
    void germanPlayersAskForGermanFacts() throws Exception {
        body = "{\"text\":\"Bananen sind Beeren\"}";

        assertEquals(Optional.of("Bananen sind Beeren"), fetch("de_de"));
        assertEquals(List.of("language=de"), queries);
    }

    @Test
    void everyOtherLanguageAsksForEnglish() throws Exception {
        body = "{\"text\":\"A fact\"}";

        for (String language : List.of("en_us", "ru_ru", "fr_fr", "ja_jp")) {
            fetch(language);
        }
        assertEquals(List.of("language=en", "language=en", "language=en", "language=en"), queries);
    }

    @Test
    void anErrorStatusFallsBack() throws Exception {
        status = 500;
        body = "{\"text\":\"should be ignored\"}";

        assertEquals(Optional.empty(), fetch("en_us"));
    }

    @Test
    void brokenJsonFallsBack() throws Exception {
        body = "{ this is not json";

        assertEquals(Optional.empty(), fetch("en_us"));
    }

    @Test
    void aBlankFactFallsBack() throws Exception {
        body = "{\"text\":\"   \"}";

        assertEquals(Optional.empty(), fetch("en_us"));
    }

    @Test
    void anUnreachableServiceFallsBackWithoutThrowing() throws Exception {
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }
        UselessFactsApiClient offline = new UselessFactsApiClient("http://127.0.0.1:" + closedPort + "/facts?language=");

        Optional<String> result = offline.fetchRandomFact("en_us").get(15, TimeUnit.SECONDS);

        assertTrue(result.isEmpty(), "an unreachable service produced a fact");
    }
}
