import type { Artist, Track, Album } from './music';

export type TopArtistSummaryResponse = {
    artist: Artist;
    tracks: Track[];
    albums: Album[];
}[];