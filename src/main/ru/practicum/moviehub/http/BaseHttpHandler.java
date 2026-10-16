package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

abstract class BaseHttpHandler implements HttpHandler {
    private static final int KEY_POSITION = 0;
    private static final int VALUE_POSITION = 1;
    private static final int MOVIES_ID_POSITION = 2;
    private static final int STATUS_NO_CONTENT = 204;
    private static final int DEFAULT_RESPONSE_LENGTH = -1;
    private static final String QUERY_SPLIT_REGEX = "&";
    private static final String PARAM_SPLIT_REGEX = "=";
    private static final String PATH_SPLIT_REGEX = "/";
    protected static final String CONTENT_TYPE = "Content-Type";
    protected static final String CT_JSON = "application/json; charset=UTF-8";

    protected void sendJson(HttpExchange ex, int status, String json) throws IOException {
        ex.getResponseHeaders().set(CONTENT_TYPE, CT_JSON);
        ex.getResponseHeaders().add("Connection", "close");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendNoContent(HttpExchange ex) throws java.io.IOException {
        ex.getResponseHeaders().set(CONTENT_TYPE, CT_JSON);
        ex.sendResponseHeaders(STATUS_NO_CONTENT, DEFAULT_RESPONSE_LENGTH);
    }

    protected Map<String, String> parseQueryParam(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new HashMap<>();
        for (String param : query.split(QUERY_SPLIT_REGEX)) {
            String[] pair = param.split(PARAM_SPLIT_REGEX);
            result.put(pair[KEY_POSITION], pair[VALUE_POSITION]);
        }
        return result;
    }

    protected String parseMoviesIdValue(String path) {
        String[] pathSplit = path.split(PATH_SPLIT_REGEX);
        if (pathSplit.length > MOVIES_ID_POSITION) {
            return pathSplit[MOVIES_ID_POSITION];
        } else {
            return "";
        }
    }
}