package com.naugroup3.interlude.models.itunes;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ITunesResponse<T> {
    private int resultCount;
    private List<T> results;

    public <Result extends ITunesResultBase> List<Result> items(Class<Result> type) {
        if (results == null) return List.of();
        return results.stream()
            .filter(type::isInstance)
            .map(type::cast)
            .toList();
    }
}
