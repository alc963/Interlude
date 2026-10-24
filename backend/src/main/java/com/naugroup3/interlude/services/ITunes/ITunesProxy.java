package com.naugroup3.interlude.services.ITunes;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

import com.fasterxml.jackson.core.type.TypeReference;
import com.naugroup3.interlude.models.itunes.ITunesArtist;
import com.naugroup3.interlude.models.itunes.ITunesProxyArtist;
import com.naugroup3.interlude.models.itunes.ITunesProxyCollection;
import com.naugroup3.interlude.models.itunes.ITunesCollection;
import com.naugroup3.interlude.models.itunes.ITunesEntity;
import com.naugroup3.interlude.models.itunes.ITunesResponse;
import com.naugroup3.interlude.models.itunes.ITunesResultBase;
import com.naugroup3.interlude.models.itunes.ITunesTrack;
import com.naugroup3.interlude.models.itunes.ITunesProxyTrack;
import com.naugroup3.interlude.utils.Expected;
import com.naugroup3.interlude.utils.Fetch;
import com.naugroup3.interlude.utils.RateLimiter;
import com.naugroup3.interlude.utils.RateLimiter.PrecheckResult;
import com.naugroup3.interlude.utils.SharedRateLimitBudget;

public class ITunesProxy {
    private static final String BASE_URL = "https://itunes.apple.com";
    private static final int REQUESTS_PER_MINUTE = 20;
    private static final int SEARCH_LIMIT = 20;
    private static final int LOOKUP_LIMIT = 200;

    private final ITunesDatabase database;

    public record AlbumQuery(Long artist_id, String name) {}
    public record TrackQuery(Long collection_id, String name) {}

    private final ScheduledExecutorService scheduler;
    private final ExecutorService http_executor;
    private final SharedRateLimitBudget budget;
    private final RateLimiter<String, List<ITunesArtist>, HttpResponse<String>> artist_search_rate_limiter;
    private final RateLimiter<AlbumQuery, List<ITunesCollection>, HttpResponse<String>> artist_albums_rate_limiter;
    private final RateLimiter<TrackQuery, List<ITunesTrack>, HttpResponse<String>> album_tracks_rate_limiter;

    public ITunesProxy(ITunesDatabase database) {
        this.database = database;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.http_executor = Executors.newCachedThreadPool();
        this.budget = new SharedRateLimitBudget(REQUESTS_PER_MINUTE, scheduler);
        // order of init matters since we want to alert tracks album tracks first
        this.album_tracks_rate_limiter = new RateLimiter<>(this.budget, this::precheck_album_tracks, this::execute_album_tracks);
        this.artist_albums_rate_limiter = new RateLimiter<>(this.budget, this::precheck_artist_albums, this::execute_artist_albums);
        // artists have no precheck; a new artist may share a name with one already stored
        this.artist_search_rate_limiter = new RateLimiter<>(this.budget, null, this::execute_search_artists);
    }

    private <Type> Expected<Type, HttpResponse<String>> get_api(String path, Map<String, Object> params, TypeReference<Type> type) {
        final String href = String.format("%s/%s", BASE_URL, path);
        final var request = HttpRequest.newBuilder().GET();
        return Fetch.request_json(href, params, request, type);
    }

    private <Result extends ITunesResultBase> Expected<List<Result>, HttpResponse<String>> search_api(Class<Result> type, String term, ITunesEntity entity, Integer limit) {
        assert limit > 0 && limit <= 200 : "Limit must be within [1-200]";
        final Map<String, Object> params = new HashMap<>();
        params.put("term", term);
        params.put("media", "music");
        params.put("limit", limit);
        params.put("entity", entity.name());
        final var response = get_api("search", params, new TypeReference<ITunesResponse<ITunesResultBase>>(){});
        if (response.has_value()) return new Expected.Success<>(response.value().items(type));
        return new Expected.Failure<>(response.error());
    }

    private <Result extends ITunesResultBase> Expected<List<Result>, HttpResponse<String>> lookup_api(Class<Result> type, List<Long> id, ITunesEntity entity, Integer limit, Boolean sort_recent) {
        assert limit > 0 && limit <= 200 : "Limit must be within [1-200]";
        final Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("limit", limit);
        params.put("entity", entity.name());
        if (sort_recent) params.put("sort", "recent");
        final var response = get_api("lookup", params, new TypeReference<ITunesResponse<ITunesResultBase>>(){});
        if (response.has_value()) return new Expected.Success<>(response.value().items(type));
        return new Expected.Failure<>(response.error());
    }
    private <Result extends ITunesResultBase> Expected<List<Result>, HttpResponse<String>> lookup_api(Class<Result> type, Long id, ITunesEntity entity, Integer limit) {
        final List<Long> ids = new ArrayList<>();
        ids.add(id);
        return lookup_api(type, ids, entity, limit, false);
    }

    private <Result> Expected<List<Result>, HttpResponse<String>> empty_result() {
        return new Expected.Success<>(List.of());
    }
    private <Result extends ITunesResultBase> Expected<List<Result>, HttpResponse<String>> clean_result(Expected<List<Result>, HttpResponse<String>> result) {
        if (result.has_value()) return result;
        final int status = result.error().statusCode();
        if (status == 429) return result;
        return empty_result();
    }

    private <ProxyType, DatabaseType> Expected<List<DatabaseType>, HttpResponse<String>> to_database_model(Expected<List<ProxyType>, HttpResponse<String>> result, Function<ProxyType, DatabaseType> convert_fn, Consumer<DatabaseType> insert_database_fn) {
        if (!result.has_value()) return new Expected.Failure<>(result.error());
        final List<DatabaseType> database_model_list = result.value().stream().map(convert_fn).toList();
        database_model_list.forEach(insert_database_fn::accept);
        return new Expected.Success<>(database_model_list);
    }

    private CompletableFuture<Expected<List<ITunesArtist>, HttpResponse<String>>> execute_search_artists(String artist) {
        return CompletableFuture
            .supplyAsync(() -> to_database_model(clean_result(search_api(ITunesProxyArtist.class, artist, ITunesEntity.musicArtist, SEARCH_LIMIT)), ITunesArtist::new, database::save_artist), http_executor)
            .exceptionally(t -> empty_result());
    }
    private CompletableFuture<Expected<List<ITunesCollection>, HttpResponse<String>>> execute_artist_albums(AlbumQuery query) {
        return CompletableFuture
            .supplyAsync(() -> to_database_model(clean_result(lookup_api(ITunesProxyCollection.class, query.artist_id(), ITunesEntity.album, LOOKUP_LIMIT)), ITunesCollection::new, database::save_album), http_executor)
            .exceptionally(t -> empty_result());
    }
    // TODO add batch executing to compact multiple collection to track requests
    private CompletableFuture<Expected<List<ITunesTrack>, HttpResponse<String>>> execute_album_tracks(TrackQuery query) {
        return CompletableFuture
            .supplyAsync(() -> to_database_model(clean_result(lookup_api(ITunesProxyTrack.class, query.collection_id(), ITunesEntity.song, LOOKUP_LIMIT)), ITunesTrack::new, database::save_track), http_executor)
            .exceptionally(t -> empty_result());
    }

    private PrecheckResult<List<ITunesCollection>> precheck_artist_albums(AlbumQuery query) {
        if (query.name() == null) return new PrecheckResult.Proceed<>();
        final Optional<ITunesCollection> cached = database.get_artist_album(query.artist_id(), query.name());
        if (cached.isPresent()) return new PrecheckResult.Resolved<>(List.of(cached.get()));
        return new PrecheckResult.Proceed<>();
    }
    private PrecheckResult<List<ITunesTrack>> precheck_album_tracks(TrackQuery query) {
        if (query.name() == null) return new PrecheckResult.Proceed<>();
        final Optional<ITunesTrack> cached = database.get_album_track(query.collection_id(), query.name());
        if (cached.isPresent()) return new PrecheckResult.Resolved<>(List.of(cached.get()));
        return new PrecheckResult.Proceed<>();
    }

    public CompletableFuture<Expected<List<ITunesArtist>, HttpResponse<String>>> search_artists(String artist) {
        return this.artist_search_rate_limiter.add(artist).future;
    }
    public CompletableFuture<Expected<List<ITunesCollection>, HttpResponse<String>>> artists_albums(Long artist_id) {
        return artists_albums(artist_id, null);
    }
    public CompletableFuture<Expected<List<ITunesCollection>, HttpResponse<String>>> artists_albums(Long artist_id, String name) {
        return this.artist_albums_rate_limiter.add(new AlbumQuery(artist_id, name)).future;
    }
    public CompletableFuture<Expected<List<ITunesTrack>, HttpResponse<String>>> album_tracks(Long collection_id) {
        return album_tracks(collection_id, null);
    }
    public CompletableFuture<Expected<List<ITunesTrack>, HttpResponse<String>>> album_tracks(Long collection_id, String name) {
        return this.album_tracks_rate_limiter.add(new TrackQuery(collection_id, name)).future;
    }

    public void shutdown() {
        this.artist_search_rate_limiter.shutdown();
        this.artist_albums_rate_limiter.shutdown();
        this.album_tracks_rate_limiter.shutdown();
        this.scheduler.shutdown();
        this.http_executor.shutdown();
    }
}
