package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.exception.*;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.model.MovieDTO;
import ru.practicum.moviehub.service.MoviesService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

public class MoviesHandler extends BaseHttpHandler {
    private static final int STATUS_OK = 200;
    private static final int STATUS_CREATED = 201;
    private static final int STATUS_UNPROCESSABLE_ENTITY = 422;
    private static final int STATUS_UNSUPPORTED_MEDIA_TYPE = 415;
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
        System.out.println("DEBUG: handle()");
        try {
            String method = ex.getRequestMethod();
            if (!ex.getRequestHeaders().containsKey(CONTENT_TYPE)) {
                throw new UnsupportedMediaTypeException("Content-Type");
            }
            if (method.equalsIgnoreCase(METHOD_GET)) {
                System.out.println("DEBUG: switchMethodGet");
                runMethodGet(ex);
            } else if (method.equalsIgnoreCase(METHOD_POST)) {
                runMethodPost(ex);
            } else if (method.equalsIgnoreCase(METHOD_DELETE)) {
                runMethodDelete(ex);
            } else {
                throw new InvalidEndPointException("Method not supported: " + method);
            }
        } catch (ValidationException e) {
            sendJson(ex, STATUS_UNPROCESSABLE_ENTITY, gson.toJson(e.getMessage()));
        } catch (UnsupportedMediaTypeException e) {
            sendJson(ex, STATUS_UNSUPPORTED_MEDIA_TYPE, gson.toJson(e.getMessage()));
        } catch (NotFoundException e) {
            sendJson(ex, STATUS_NOT_FOUND, gson.toJson(e.getMessage()));
        } catch (BadRequestException | InvalidEndPointException e) {
            sendJson(ex, STATUS_BAD_REQUEST, gson.toJson(e.getMessage()));
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
        System.out.println("DEBUG: runMethodGet() path: " + path + ", query: " + query);
        Map<String, String> queryMap = parseQueryParam(query);
        String id = parseMoviesIdValue(path);
        System.out.println("DEBUG: queryMap: " + queryMap + ", id: " + id);
        if ((id == null || id.isEmpty()) && queryMap.isEmpty()) {
            System.out.println("DEBUG: должен был попасть сюда");
            Collection<Movie> movies = moviesService.getMovies();
            String moviesGson = gson.toJson(movies);
            sendJson(ex, STATUS_OK, moviesGson);
        } else if (id != null && !id.isEmpty()) {
            int movieId = parseInt(id);
            Movie movie = moviesService.getMovie(movieId);
            String movieGson = gson.toJson(movie);
            sendJson(ex, STATUS_OK, movieGson);
        } else {
            String stringYear = queryMap.get(PARAM_YEAR);
            int year = parseInt(stringYear);
            Collection<Movie> movies = moviesService.getMoviesByYear(year);
            String moviesGson = gson.toJson(movies);
            sendJson(ex, STATUS_OK, moviesGson);
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