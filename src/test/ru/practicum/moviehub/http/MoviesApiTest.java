package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.MovieDTO;
import ru.practicum.moviehub.service.MoviesServiceImpl;
import ru.practicum.moviehub.store.MoviesStore;
import ru.practicum.moviehub.store.MoviesStoreImpl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesStore store;
    private static Gson gson = new Gson();

    @BeforeAll
    static void beforeAll() {
        store = new MoviesStoreImpl();
        server = new MoviesServer(new MoviesServiceImpl(store), 8080);
        server.start();
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    }

    @BeforeEach
    void beforeEach() {
        store.removeAllMovies();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен быть application/json; charset=UTF-8");

        String body = resp.body().trim();
        assertEquals("[]", body, "Ожидается пустой JSON-массив");
    }

    @Test
    void getMovies_whenHasMovies_returnsList() throws Exception {
        store.addMovie(new MovieDTO("Dune", 2021));
        store.addMovie(new MovieDTO("The Matrix", 1999));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().allValues("Content-Type").getFirst();
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue);

        JsonArray moviesArray = gson.fromJson(resp.body(), JsonArray.class);
        assertEquals(2, moviesArray.size(), "Ожидается 2 фильма");

        JsonObject firstMovie = moviesArray.get(0).getAsJsonObject();
        assertEquals(1, firstMovie.get("id").getAsInt());
        assertEquals("Dune", firstMovie.get("title").getAsString());
        assertEquals(2021, firstMovie.get("year").getAsInt());

        JsonObject secondMovie = moviesArray.get(1).getAsJsonObject();
        assertEquals(2, secondMovie.get("id").getAsInt());
        assertEquals("The Matrix", secondMovie.get("title").getAsString());
        assertEquals(1999, secondMovie.get("year").getAsInt());
    }

    @Test
    void getMovies_whenUnsupportedMethod_returns405() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .method("PATCH", BodyPublishers.noBody())
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(405, resp.statusCode(), "PATCH /movies должен вернуть 405");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue);

        JsonObject errorResponse = gson.fromJson(resp.body(), JsonObject.class);
        assertEquals("Method not allowed", errorResponse.get("error").getAsString());
        assertTrue(errorResponse.has("details"));
    }
}
