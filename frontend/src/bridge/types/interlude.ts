import type { Artist, Track, Album } from './music';

export type TopArtistSummaryResponse = {
    artist: Artist;
    tracks: Track[];
    albums: Album[];
}[];
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
