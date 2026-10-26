export type Artist = {
    id?: string;
    name: string;
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