import { useEffect, useState } from 'react';
import { ApiBridge } from '../bridge/api_bridge';
import type { TopArtistsWithSummary } from '../bridge/types/interlude';

const TOP_ARTIST_COUNT = 5;
const SUMMARY_LIMIT = 3;

export default function useTopArtistsSummary() {
    const [topArtistsSummary, setTopArtistsSummary] = useState<TopArtistsWithSummary | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let isCurrent = true;

        async function loadTopArtistsSummary() {
            try {
                const artists = await ApiBridge.top_artists({ limit: TOP_ARTIST_COUNT });
                if (artists instanceof Response) {
                    throw new Error(`Failed to load top artists (${artists.status}).`);
                }
                const summaries = await Promise.all(artists.map(async (artist) => {
                    const summary = await ApiBridge.top_artist_summary({ id: artist.id, limit: SUMMARY_LIMIT });
                    if (summary instanceof Response) {
                        throw new Error(`Failed to load top artists (${summary.status}).`);
                    }
                    return { artist, ...summary };
                }));
                if (isCurrent) setTopArtistsSummary(summaries);
            } catch (reason) {
                if (isCurrent) {
                    setError(reason instanceof Error ? reason.message : 'Failed to load top artists.');
                }
            } finally {
                if (isCurrent) setLoading(false);
            }
        }

        void loadTopArtistsSummary();
        return () => {
            isCurrent = false;
        };
    }, []);

    return { topArtistsSummary, loading, error };
}
