package io.kimconsulting.maxbots.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.kimconsulting.maxbots.api.http.MaxApiClient;
import io.kimconsulting.maxbots.api.model.Message;
import io.kimconsulting.maxbots.api.model.NewMessageBody;
import io.kimconsulting.maxbots.api.request.SendMessageRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MaxApiClientTest {
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void sendsAuthorizationHeaderAndQueryParameters() throws Exception {
        AtomicReference<String> authHeader = new AtomicReference<>();
        AtomicReference<String> requestUri = new AtomicReference<>();
        AtomicReference<String> requestBody = new AtomicReference<>();

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/messages", exchange -> {
            authHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            requestUri.set(exchange.getRequestURI().toString());
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, """
                {
                  "body": {
                    "mid": "mid-1",
                    "text": "Привет"
                  },
                  "recipient": {
                    "chat_id": 42
                  }
                }
                """);
        });
        server.start();

        MaxApiClient client = MaxApiClient.builder("test-token")
            .baseUri(new URI("http://127.0.0.1:" + server.getAddress().getPort()))
            .build();

        Message message = client.sendMessage(SendMessageRequest.toChat(42L, NewMessageBody.text("Привет")));

        Assertions.assertEquals("test-token", authHeader.get());
        Assertions.assertTrue(requestUri.get().contains("chat_id=42"));
        Assertions.assertTrue(requestBody.get().contains("\"text\":\"Привет\""));
        Assertions.assertEquals("mid-1", message.body().mid());
    }

    private static void respond(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }
}
