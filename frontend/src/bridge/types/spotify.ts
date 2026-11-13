export type SpotifyExternalUrls = { spotify: string };

export type SpotifyImage = {
    url: string;
    height: number | null;
    width: number | null;
};

export type SpotifyRestrictions = { reason: string };

export type SpotifyArtist = {
    external_urls: SpotifyExternalUrls;
    href: string;
    id: string;
    name: string;
    type: "artist";
    uri: string;
};

export type SpotifyAlbum = {
    album_type: "album" | "single" | "compilation";
    total_tracks: number;
    external_urls: SpotifyExternalUrls;
    href: string;
    id: string;
    images: SpotifyImage[];
    name: string;
    release_date: string;
    release_date_precision: "year" | "month" | "day";
    restrictions: SpotifyRestrictions | null;
    type: "album";
    uri: string;
    artists: SpotifyArtist[];
};

export type SpotifyTrack = {
    album: SpotifyAlbum;
    artists: SpotifyArtist[];
    disc_number: number;
    duration_ms: number;
    explicit: boolean;
    external_ids: { isrc: string | null; ean: string | null; upc: string | null } | null;
    external_urls: SpotifyExternalUrls;
    href: string;
    id: string;
    is_playable: boolean | null;
    restrictions: SpotifyRestrictions | null;
    name: string;
    track_number: number;
    type: "track";
    uri: string;
    is_local: boolean;
};

export type SpotifyContext = {
    type: "artist" | "playlist" | "album" | "show" | string;
    href: string;
    external_urls: SpotifyExternalUrls;
    uri: string;
};

export type SpotifyDevice = {
    id: string | null;
    is_active: boolean;
    is_private_session: boolean;
    is_restricted: boolean;
    name: string;
    type: string;
    volume_percent: number | null;
    supports_volume: boolean;
};

export type CurrentlyPlayingResponse = {
    device: SpotifyDevice | null;
    repeat_state: "off" | "track" | "context" | null;
    shuffle_state: boolean | null;
    context: SpotifyContext | null;
    timestamp: number | null;
    progress_ms: number | null;
    is_playing: boolean;
    item: SpotifyTrack | null;
    currently_playing_type: "track" | "episode" | "ad" | "unknown" | null;
};

export type RecentlyPlayedResponse = {
    href: string;
    limit: number;
    next: string | null;
    cursors: { after: string; before: string } | null;
    total: number | null;
    items: {
        track: SpotifyTrack;
        played_at: string;
        context: SpotifyContext | null;
    }[];
};

export type RecentlyPlayedRequest = {
    limit?: number;
    after?: number;
    before?: number;
};
