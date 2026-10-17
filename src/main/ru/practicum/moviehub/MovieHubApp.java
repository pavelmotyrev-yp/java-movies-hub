package ru.practicum.moviehub;

import ru.practicum.moviehub.http.MainController;
import ru.practicum.moviehub.service.MoviesServiceImpl;
import ru.practicum.moviehub.store.MoviesStoreImpl;

public class MovieHubApp {
    public static void main(String[] args) {
        final MainController server = new MainController(new MoviesServiceImpl(new MoviesStoreImpl()), 8080);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
    }
}