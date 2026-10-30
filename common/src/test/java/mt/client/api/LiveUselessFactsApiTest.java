package mt.client.api;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class LiveUselessFactsApiTest {

    private static final String HOST = "uselessfacts.jsph.pl";

    private static boolean online() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(HOST, 443), 3000);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private static void expectFact(String language) throws Exception {
        assumeTrue(online(), HOST + " is not reachable from here, the live check is skipped");
        Optional<String> fact = new UselessFactsApiClient().fetchRandomFact(language).get(15, TimeUnit.SECONDS);
        assertTrue(fact.isPresent() && !fact.get().isBlank(), "the live service returned no fact for " + language);
    }

    @Test
    void theLiveServiceAnswersInEnglish() throws Exception {
        expectFact("en_us");
    }

    @Test
    void theLiveServiceAnswersInGerman() throws Exception {
        expectFact("de_de");
    }
}
