package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.exception.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080";
    private static MainController server;
    private static HttpClient client;
    private static MoviesStore store;
    private static Gson gson = new Gson();

    @BeforeAll
    static void beforeAll() {
        store = new MoviesStoreImpl();
        server = new MainController(new MoviesServiceImpl(store), 8080);
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

        String body = resp.body();
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

        List<Movie> moviesArray = gson.fromJson(resp.body(), new ListOfMoviesTypeToken().getType());
        assertEquals(2, moviesArray.size(), "Ожидается 2 фильма");

        Movie firstMovie = moviesArray.get(0);
        assertNotEquals(0, firstMovie.id());
        assertEquals("Dune", firstMovie.title());
        assertEquals(2021, firstMovie.year());

        Movie secondMovie = moviesArray.get(1);
        assertNotEquals(0, secondMovie.id());
        assertEquals("The Matrix", secondMovie.title());
        assertEquals(1999, secondMovie.year());
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

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Method not allowed", errorResponse.getError());
        assertTrue(errorResponse.getDetails().getFirst().equalsIgnoreCase("Method: PATCH"));
    }

    @Test
    void addMovie_whenValid_returns201() throws Exception {
        String requestBody = "{\"title\": \"Dune\", \"year\": 2021}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue);

        Movie movie = gson.fromJson(resp.body(), Movie.class);
        assertNotEquals(0, movie.id(), "id должен быть присвоен");
        assertEquals("Dune", movie.title());
        assertEquals(2021, movie.year());
    }

    @Test
    void addMovie_whenEmptyTitle_returns422() throws Exception {
        String requestBody = "{\"title\": \"\", \"year\": 2021}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies с пустым title должен вернуть 422");

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertTrue(errorResponse.getError().startsWith("Title is empty: "));
        assertEquals(1, errorResponse.getDetails().size());
        assertTrue(errorResponse.getDetails().getFirst().startsWith("Title is empty: "));
    }

    @Test
    void addMovie_whenTitleTooLong_returns422() throws Exception {
        String longTitle = "a".repeat(101);
        String requestBody = "{\"title\": \"" + longTitle + "\", \"year\": 2021}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies с title > 100 должен вернуть 422");

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertTrue(errorResponse.getError().startsWith("Title is too long: "));
        assertEquals(1, errorResponse.getDetails().size());
        assertTrue(errorResponse.getDetails().getFirst().startsWith("Title is too long: "));
    }

    @Test
    void addMovie_whenYearTooOld_returns422() throws Exception {
        String requestBody = "{\"title\": \"Test\", \"year\": 1879}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies с year < 1888 должен вернуть 422");

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertTrue(errorResponse.getError().startsWith("Year should be more than "));
        assertEquals(1, errorResponse.getDetails().size());
        assertTrue(errorResponse.getDetails().getFirst().startsWith("Year should be more than "));
    }

    @Test
    void addMovie_whenYearTooFar_returns422() throws Exception {
        String requestBody = "{\"title\": \"Test\", \"year\": 2027}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies с слишком большим year должен вернуть 422");

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertTrue(errorResponse.getError().startsWith("Year should be less than "));
        assertEquals(1, errorResponse.getDetails().size());
        assertTrue(errorResponse.getDetails().getFirst().startsWith("Year should be less than "));
    }

    @Test
    void addMovie_whenWrongContentType_returns415() throws Exception {
        String requestBody = "{\"title\": \"Test\", \"year\": 2021}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "text/plain")
                .POST(BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(415, resp.statusCode(), "POST /movies с неправильным Content-Type должен вернуть 415");

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Unsupported Media Type", errorResponse.getError());
        assertEquals(1, errorResponse.getDetails().size());
        assertEquals("Content-Type must be application/json", errorResponse.getDetails().getFirst());
    }

    @Test
    void addMovie_whenInvalidJson_returns400() throws Exception {
        String requestBody = "not valid json{";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "POST /movies с некорректным JSON должен вернуть 400");

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Invalid JSON", errorResponse.getError());
        assertEquals(1, errorResponse.getDetails().size());
    }

    @Test
    void addMovie_whenInvalidDataInJson_returns400() throws Exception {
        String requestBody = "{\"title\": \"Dune\", \"year\": year}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "POST /movies с некорректным JSON должен вернуть 400");

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals("Invalid JSON", errorResponse.getError());
        assertEquals(1, errorResponse.getDetails().size());
    }

    @Test
    void getMovie_whenExists_returnsMovie() throws Exception {
        store.addMovie(new MovieDTO("Dune", 2021));

        Movie movie = store.getMovies().stream().findFirst().get();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + movie.id()))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies/1 должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue);

        Movie recivedMovie = gson.fromJson(resp.body(), Movie.class);
        assertEquals(movie.id(), recivedMovie.id());
        assertEquals("Dune", recivedMovie.title());
        assertEquals(2021, recivedMovie.year());
    }

    @Test
    void getMovie_whenNotFound_returns404() throws Exception {
        store.addMovie(new MovieDTO("Dune", 2021));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/99999"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode(), "GET /movies/99999 должен вернуть 404");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue);

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertTrue(errorResponse.getError().startsWith("Movie not found by id: "));
        assertEquals(1, errorResponse.getDetails().size());
        assertTrue(errorResponse.getDetails().getFirst().startsWith("Movie not found by id: "));
    }

    @Test
    void getMovie_whenInvalidId_returns400() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/abc"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "GET /movies/abc должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue);

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertTrue(errorResponse.getError().startsWith("Value is not a number: "));
        assertEquals(1, errorResponse.getDetails().size());
        assertTrue(errorResponse.getDetails().getFirst().startsWith("Value is not a number: "));
    }

    @Test
    void deleteMovie_whenExists_returns204() throws Exception {
        store.addMovie(new MovieDTO("Dune", 2021));

        Movie movie = store.getMovies().stream().findFirst().get();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" +  movie.id()))
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, resp.statusCode(), "DELETE /movies/" + movie.id() + " должен вернуть 204");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue);
    }

    @Test
    void deleteMovie_whenNotFound_returns404() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/99999"))
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode(), "DELETE /movies/99999 должен вернуть 404");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue);

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertTrue(errorResponse.getError().startsWith("Movie not found by id: "));
        assertEquals(1, errorResponse.getDetails().size());
        assertTrue(errorResponse.getDetails().getFirst().startsWith("Movie not found by id: "));
    }

    @Test
    void deleteMovie_whenInvalidId_returns400() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/abc"))
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "DELETE /movies/abc должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue);

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertTrue(errorResponse.getError().startsWith("Value is not a number: "));
        assertEquals(1, errorResponse.getDetails().size());
        assertTrue(errorResponse.getDetails().getFirst().startsWith("Value is not a number: "));
    }

    @Test
    void getMoviesByYear_whenExists_returnsMovies() throws Exception {
        store.addMovie(new MovieDTO("Dune", 2021));
        store.addMovie(new MovieDTO("The Matrix", 1999));
        store.addMovie(new MovieDTO("Dune: Part Two", 2021));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2021"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies?year=2021 должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue);

        List<Movie> movies = gson.fromJson(resp.body(), new ListOfMoviesTypeToken().getType());
        assertEquals(2, movies.size(), "Ожидается 2 фильма за 2021 год");
        assertTrue(movies.stream().allMatch(m -> m.year() == 2021));
    }

    @Test
    void getMoviesByYear_whenNoneFound_returnsEmptyList() throws Exception {
        store.addMovie(new MovieDTO("Dune", 2021));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2000"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies?year=2000 должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue);

        List<Movie> movies = gson.fromJson(resp.body(), new ListOfMoviesTypeToken().getType());
        assertTrue(movies.isEmpty(), "Ожидается пустой список");
    }

    @Test
    void getMoviesByYear_whenInvalidYear_returns400() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=abc"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "GET /movies?year=abc должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue);

        ErrorResponse errorResponse = gson.fromJson(resp.body(), ErrorResponse.class);
        assertTrue(errorResponse.getError().startsWith("Value is not a number: "));
        assertEquals(1, errorResponse.getDetails().size());
        assertTrue(errorResponse.getDetails().getFirst().startsWith("Value is not a number: "));
    }
}
