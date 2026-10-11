package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.exception.BadRequestException;
import ru.practicum.moviehub.exception.ErrorResponse;
import ru.practicum.moviehub.exception.NotFoundException;
import ru.practicum.moviehub.exception.ValidationException;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.model.MovieDTO;
import ru.practicum.moviehub.service.MoviesService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MoviesHandler extends BaseHttpHandler {
    private static final int STATUS_OK = 200;
    private static final int STATUS_CREATED = 201;
    private static final int STATUS_UNPROCESSABLE_ENTITY = 422;
    private static final int STATUS_METHOD_NOT_ALLOWED = 405;
    private static final int STATUS_NOT_FOUND = 404;
    private static final int STATUS_BAD_REQUEST = 400;
    private static final String PARAM_YEAR = "year";
    private static final String PARAM_TITLE = "title";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_DELETE = "DELETE";


    private MoviesService moviesService;
    private Gson gson;

    public MoviesHandler(MoviesService moviesService) {
        this.moviesService = moviesService;
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();
            if (method.equalsIgnoreCase(METHOD_GET)) {
                runMethodGet(ex);
            } else if (method.equalsIgnoreCase(METHOD_POST)) {
                runMethodPost(ex);
            } else if (method.equalsIgnoreCase(METHOD_DELETE)) {
                runMethodDelete(ex);
            } else {
                ErrorResponse errorResponse = new ErrorResponse("Method not allowed", List.of("Method: " + method));
                sendJson(ex, STATUS_METHOD_NOT_ALLOWED, gson.toJson(errorResponse));
            }
        } catch (ValidationException e) {
            ErrorResponse errorResponse = new ErrorResponse("Validation failed", List.of(e.getMessage()));
            sendJson(ex, STATUS_UNPROCESSABLE_ENTITY, gson.toJson(errorResponse));
        } catch (NotFoundException e) {
            ErrorResponse errorResponse = new ErrorResponse("Not found", List.of(e.getMessage()));
            sendJson(ex, STATUS_NOT_FOUND, gson.toJson(errorResponse));
        } catch (BadRequestException e) {
            ErrorResponse errorResponse = new ErrorResponse("Bad request", List.of(e.getMessage()));
            sendJson(ex, STATUS_BAD_REQUEST, gson.toJson(errorResponse));
        }
    }

    private void runMethodDelete(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String id = parseMoviesIdValue(path);
        int movieId = parseInt(id);
        moviesService.removeMovie(movieId);
        sendNoContent(ex);
    }

    private void runMethodPost(HttpExchange ex) throws IOException {
        String requestBody = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JsonElement jsonElement = JsonParser.parseString(requestBody);
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String title = jsonObject.get(PARAM_TITLE).getAsString();
            int year = jsonObject.get(PARAM_YEAR).getAsInt();
            MovieDTO movieDTO = new MovieDTO(title, year);
            Movie movie = moviesService.addMovie(movieDTO);
            sendJson(ex, STATUS_CREATED, gson.toJson(movie));
        } else {
            throw new ValidationException("Invalid request body: " + requestBody);
        }
    }

    private void runMethodGet(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String query = ex.getRequestURI().getQuery();
        Map<String, String> queryMap = parseQueryParam(query);
        String id = parseMoviesIdValue(path);
        if ((id == null || id.isEmpty()) && queryMap.isEmpty()) {
            Collection<Movie> movies = moviesService.getMovies();
            sendJson(ex, STATUS_OK, gson.toJson(movies));
        } else if (id != null && !id.isEmpty()) {
            int movieId = parseInt(id);
            Movie movie = moviesService.getMovie(movieId);
            sendJson(ex, STATUS_OK, gson.toJson(movie));
        } else {
            String stringYear = queryMap.get(PARAM_YEAR);
            int year = parseInt(stringYear);
            Collection<Movie> movies = moviesService.getMoviesByYear(year);
            sendJson(ex, STATUS_OK, gson.toJson(movies));
        }
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Value is not a number: " + value);
        }
    }

}