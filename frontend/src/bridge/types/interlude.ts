export type TopArtistRef = {
    id: string | null;
    name: string;
};

export type TopArtist = {
    id: string;
    name: string;
    artworkUrl: string | null;
    playCount: number;
};

export type TopAlbum = {
    id: string;
    name: string;
    artists: TopArtistRef[];
    artworkUrl: string | null;
    playCount: number;
};

export type TopTrack = {
    id: string;
    name: string;
    artists: TopArtistRef[];
    album: string | null;
    duration: number | null; // seconds
    playCount: number;
    rank: number;
    artworkUrl: string | null;
};

export type TopArtistSummaryRequest = {
    id: string; // spotify artist uri, from top_artists
    limit?: number; // default 5; 1-50
};

export type TopArtistSummaryResponse = {
    tracks: TopTrack[];
    albums: TopAlbum[];
};

export type TopArtistsWithSummary = ({ artist: TopArtist } & TopArtistSummaryResponse)[];
export type TopItemsRequest = {
    limit?: number;
};

export type TopTracksResponse = TopTrack[];
export type TopArtistsResponse = TopArtist[];
export type TopAlbumsResponse = TopAlbum[];

export type RecommendationsRequest = {
    limit?: number; // default 20; 1-50
};

export type RecommendedTrack = {
    id: number;
    name: string;
    artist: string | null;
    album: string | null;
    genre: string | null;
    mood: string;
    artwork_url: string | null;
    preview_url: string | null;
    view_url: string;
    score: number;
};

export type RecommendationsResponse = RecommendedTrack[];
