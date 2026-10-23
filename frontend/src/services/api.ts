import axios from 'axios';
import type { Artist, Track, Album } from '../util/types';

const topEndpoint = '/me/top/';

export async function getTopTracks(amount: Number): Promise<Track[]> {
    const { data } = await axios.get<Track[]>(topEndpoint + 'tracks', {
        params: {
            amount: amount,
        }
    });
    return data;
}

export async function getTopArtists(amount: Number): Promise<Artist[]> {
    const { data } = await axios.get<Artist[]>(topEndpoint + 'artists', {
        params: {
            amount: amount,
        }
    });
    return data;
}

export async function getTopAlbums(amount: Number): Promise<Album[]> {
    const { data } = await axios.get<Album[]>(topEndpoint + 'albums', {
        params: {
            amount: amount,
        }
    });
    return data;
}
