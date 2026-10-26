export type CurrentlyPlayingResponse = {
    is_playing: boolean;
    progress_ms: number | null;
    item: {
        name: string;
        artists: { name: string }[];
        album: {
            images: { url : string }[];
        };
    } | null;
}