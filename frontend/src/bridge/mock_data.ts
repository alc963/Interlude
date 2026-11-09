import type { TopArtistSummaryResponse } from './types/interlude';

const mockArtists = [
    { id: 'artist-1', name: 'Mira Vale', color: '264653' },
    { id: 'artist-2', name: 'King Gizzard & the Lizard Wizard', color: '2A9D8F' },
    { id: 'artist-3', name: 'Northbound', color: 'E76F51' },
    { id: 'artist-4', name: 'Solstice Arcade', color: '6D597A' },
    { id: 'artist-5', name: 'Juniper Static', color: '457B9D' },
] as const;

export const mockTopArtistSummary: TopArtistSummaryResponse = mockArtists.map((mockArtist, artistIndex) => {
    const artist = {
        id: mockArtist.id,
        name: mockArtist.name,
        artistArtUrl: `https://placehold.co/300x300/${mockArtist.color}/FFFFFF?text=${encodeURIComponent(mockArtist.name)}`,
    };

    const albums = [1, 2, 3].map((albumNumber) => ({
        id: `album-${artistIndex + 1}-${albumNumber}`,
        name: `${mockArtist.name} Album ${albumNumber}`,
        artists: [artist],
        albumArtUrl: `https://placehold.co/300x300/${mockArtist.color}/FFFFFF?text=${encodeURIComponent(`${mockArtist.name} Album ${albumNumber}`)}`,
        playCount: albumNumber * 291 - artistIndex * 21,
    }));

    const tracks = [1, 2, 3].map((trackNumber) => ({
        id: `track-${artistIndex + 1}-${trackNumber}`,
        name: `${mockArtist.name} Track ${trackNumber}`,
        artists: [artist],
        album: `${mockArtist.name} Album ${trackNumber}`,
        duration: 180 + trackNumber * 12,
        playCount: (5 - artistIndex) * 249 - trackNumber * 57,
        rank: trackNumber,
    }));

    return { artist, tracks, albums };
});