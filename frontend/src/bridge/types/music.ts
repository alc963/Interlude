export type Artist = {
    id?: string;
    name: string;
    artistArtUrl?: string;
};

export type Album = {
    id?: string;
    name: string;
    artists: Artist[];
    albumArtUrl?: string;
};

export type Track = {
    id?: string;
    name: string;
    artists: Artist[];
    album: string;
    duration?: number;
    playCount?: number;
    rank?: number;
    albumArtUrl?: string;
};

export function getGenreColor(genre: string) {
    return genreColors[genre.toLowerCase()] ?? genreColors['fallbackColor'];
};

const genreColors: Record<string, string> = {
    'fallbackColor': '#808080',
    'pop': '#FF6B6B',
    'rock': '#4ECDC4',
    'jazz': '#FFD166',
    'hip-hop': '#06D6A0',
    'indie rock': '#C77DFF',
    'electronic': '#5C7CFA',
    'country': '#F4A261',
    'folk': '#8D99AE',
    'blues': '#2EC4B6',
};