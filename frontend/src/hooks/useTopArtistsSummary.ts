import { useEffect, useState } from 'react';
import { ApiBridge } from '../bridge/api_bridge';
import type { TopArtistSummaryResponse } from '../bridge/types/interlude';

export default function useTopArtistsSummary() {
    const [topArtistsSummary, setTopArtistsSummary] = useState<TopArtistSummaryResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let isCurrent = true;

        async function loadTopArtistsSummary() {
            try {
                const response = await ApiBridge.top_artists_summary();
                if (response instanceof Response) {
                    throw new Error(`Failed to load top artists (${response.status}).`);
                }
                if (isCurrent) setTopArtistsSummary(response);
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
