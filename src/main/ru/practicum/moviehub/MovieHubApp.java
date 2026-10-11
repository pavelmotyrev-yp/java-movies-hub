package ru.practicum.moviehub;

import ru.practicum.moviehub.http.MoviesServer;
import ru.practicum.moviehub.service.MoviesServiceImpl;
import ru.practicum.moviehub.store.MoviesStoreImpl;

public class MovieHubApp {
    public static void main(String[] args) {
        final MoviesServer server = new MoviesServer(new MoviesServiceImpl(new MoviesStoreImpl()), 8080);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
    }
}