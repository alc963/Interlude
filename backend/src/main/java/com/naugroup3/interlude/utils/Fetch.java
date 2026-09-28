package com.naugroup3.interlude.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.naugroup3.interlude.models.itunes.ITunesResultBase;
import com.naugroup3.interlude.models.itunes.ITunesResultDeserializer;

public class Fetch {
    private static final ObjectMapper object_mapper = new ObjectMapper()
        .registerModule(new SimpleModule().addDeserializer(ITunesResultBase.class, new ITunesResultDeserializer()));
    private static final HttpClient client = HttpClient.newHttpClient();

    private static String encode_params(Map<String, Object> params) {
        final List<String> encoded_params = new ArrayList<>();
        for(Map.Entry<String, Object> entry : params.entrySet()) {
            final Object evalue = entry.getValue();
            if (evalue == null) continue;

            final String evalue_str = evalue instanceof Collection<?> collection
                ? collection.stream().map(String::valueOf).collect(Collectors.joining(","))
                : String.valueOf(evalue);

            final String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            final String value = URLEncoder.encode(evalue_str, StandardCharsets.UTF_8);
            encoded_params.add(String.format("%s=%s", key, value));
        }
        return String.join("&", encoded_params);
    }

    private static HttpResponse<String> send_request(HttpRequest request) {
        try {
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response;
        } catch (IOException | InterruptedException e) {
            return null;
        }
    } 

    public static HttpResponse<String> request(String base_url, Map<String, Object> params, HttpRequest.Builder request_builder) {
        final HttpRequest request = request_builder
            .uri(URI.create(String.format("%s?%s", base_url, encode_params(params))))
            .build();
        return send_request(request);
    }
    public static HttpResponse<String> request(String base_url, HttpRequest.Builder request_builder) {
        return request(base_url, new HashMap<>(), request_builder);
    }

    public static <T> Expected<T, HttpResponse<String>> request_json(
            String base_url, Map<String, Object> params, 
            HttpRequest.Builder request_builder, TypeReference<T> type) {
        request_builder.header("Accept", "application/json");
        final HttpResponse<String> response = request(base_url, params, request_builder);
        try {
            final T value = object_mapper.readValue(response.body(), type);
            return new Expected.Success<>(value);
        } catch(JsonProcessingException e) {
            return new Expected.Failure<>(response);
        }
    }
    public static <T> Expected<T, HttpResponse<String>> request_json(String base_url, HttpRequest.Builder request_builder, TypeReference<T> type) {
        return request_json(base_url, new HashMap<>(), request_builder, type);
    }
}
