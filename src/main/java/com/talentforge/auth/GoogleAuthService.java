package com.talentforge.auth;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Google Sign-In using the OAuth 2.0 "Authorization Code" flow for desktop apps.
 * Uses only the standard JDK (java.net.http, com.sun.net.httpserver) — no extra libraries.
 *
 * SETUP REQUIRED before this will work:
 * 1. Go to https://console.cloud.google.com/apis/credentials
 * 2. Create an OAuth Client ID of type "Desktop app"
 * 3. Copy the Client ID and Client Secret into CLIENT_ID / CLIENT_SECRET below
 *
 * How it works:
 * 1. Opens the system browser to Google's consent screen
 * 2. Starts a tiny local web server on REDIRECT_PORT to catch the redirect
 * 3. Exchanges the returned code for an access token
 * 4. Fetches the user's email/name with that token
 */
public class GoogleAuthService {

    // TODO: paste your own credentials from Google Cloud Console here
    private static final String CLIENT_ID = "584246908901-5u1l7gdi761gf2jftu15lohfnr8kbfsd.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";

    private static final int REDIRECT_PORT = 8934;
    private static final String REDIRECT_URI = "http://localhost:" + REDIRECT_PORT + "/callback";

    public static class GoogleUser {
        public final String email;
        public final String name;

        public GoogleUser(String email, String name) {
            this.email = email;
            this.name = name;
        }
    }

    /**
     * Starts the sign-in flow. Returns a CompletableFuture that completes
     * with the signed-in user's info, or completes exceptionally if the
     * user cancels or something goes wrong.
     */
    public CompletableFuture<GoogleUser> signIn() {
        CompletableFuture<GoogleUser> future = new CompletableFuture<>();

        if (CLIENT_ID.startsWith("YOUR_CLIENT_ID")) {
            future.completeExceptionally(new IllegalStateException(
                    "Google Sign-In isn't configured yet. Add your Client ID and Secret "
                            + "in GoogleAuthService.java (see setup instructions in the file comments)."));
            return future;
        }

        String state = randomState();

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(REDIRECT_PORT), 0);
            server.createContext("/callback", exchange -> handleCallback(exchange, state, future, server));
            server.start();

            String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                    + "?client_id=" + enc(CLIENT_ID)
                    + "&redirect_uri=" + enc(REDIRECT_URI)
                    + "&response_type=code"
                    + "&scope=" + enc("openid email profile")
                    + "&state=" + enc(state);

            Desktop.getDesktop().browse(URI.create(authUrl));

        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    private void handleCallback(HttpExchange exchange, String expectedState,
                                 CompletableFuture<GoogleUser> future, HttpServer server) {
        try {
            String query = exchange.getRequestURI().getQuery();
            String code = paramValue(query, "code");
            String state = paramValue(query, "state");

            String response = "<html><body style='font-family:sans-serif;text-align:center;padding:60px;'>"
                    + "<h2>You can close this tab now</h2><p>Return to TalentForge to continue.</p></body></html>";
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }

            if (code == null || !expectedState.equals(state)) {
                future.completeExceptionally(new IllegalStateException("Google sign-in failed or was cancelled."));
            } else {
                GoogleUser user = exchangeCodeForUser(code);
                future.complete(user);
            }
        } catch (Exception e) {
            future.completeExceptionally(e);
        } finally {
            new Thread(() -> server.stop(0)).start();
        }
    }

    private GoogleUser exchangeCodeForUser(String code) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        String tokenBody = "code=" + enc(code)
                + "&client_id=" + enc(CLIENT_ID)
                + "&client_secret=" + enc(CLIENT_SECRET)
                + "&redirect_uri=" + enc(REDIRECT_URI)
                + "&grant_type=authorization_code";

        HttpRequest tokenRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(tokenBody))
                .build();

        HttpResponse<String> tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
        String accessToken = jsonStringValue(tokenResponse.body(), "access_token");

        if (accessToken == null) {
            throw new IOException("Failed to get access token: " + tokenResponse.body());
        }

        HttpRequest userInfoRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://www.googleapis.com/oauth2/v3/userinfo"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> userInfoResponse = client.send(userInfoRequest, HttpResponse.BodyHandlers.ofString());
        String email = jsonStringValue(userInfoResponse.body(), "email");
        String name = jsonStringValue(userInfoResponse.body(), "name");

        if (email == null) {
            throw new IOException("Failed to get user info: " + userInfoResponse.body());
        }
        return new GoogleUser(email, name != null ? name : email);
    }

    // --- small helpers (no external JSON/HTTP library needed) ---

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String randomState() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String paramValue(String query, String key) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                return kv[1];
            }
        }
        return null;
    }

    /** Minimal flat-JSON string value extractor — avoids needing a JSON library dependency. */
    private static String jsonStringValue(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }
}