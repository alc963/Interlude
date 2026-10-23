import axios from 'axios';
import type { Artist, Track, Album } from '../util/music';

const topEndpoint = '/me/top/';

export async function get(endpoint: string) {
    console.log(endpoint);
}

export async function getTopTracks(amount: number): Promise<Track[]> {
    const { data } = await axios.get<Track[]>(topEndpoint + 'tracks', {
        params: {
            amount: amount,
        }
    });
    return data;
}

export async function getTopArtists(amount: number): Promise<Artist[]> {
    const { data } = await axios.get<Artist[]>(topEndpoint + 'artists', {
        params: {
            amount: amount,
        }
    });
    return data;
}

export async function getTopAlbums(amount: number): Promise<Album[]> {
    const { data } = await axios.get<Album[]>(topEndpoint + 'albums', {
        params: {
            amount: amount,
        }
    });
    return data;
}
