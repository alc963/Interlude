import { useEffect, useState, type ReactNode } from 'react';
import { ApiBridge } from '../../bridge/api_bridge';
import type { TopArtistsResponse } from '../../bridge/types/interlude';
import Table from './Table';

type ArtistsWidgetProps = {
  artistAmount?: number;
};

type ArtistRow = {
  pic: ReactNode;
  artist: string;
  playcount: number;
};

const columns = [
  { key: 'pic', label: '' },
  { key: 'artist', label: 'Artists' },
  { key: 'playcount', label: 'Plays' },
];

const MAX_ROWS = 10;

async function getTopArtists(limit: number): Promise<TopArtistsResponse> {
  const artists = await ApiBridge.top_artists({ limit });
  if (artists instanceof Response) {
    throw new Error(`Failed to load top artists (${artists.status})`);
  }
  return artists;
}

function ArtistsWidget({ artistAmount = 5 }: ArtistsWidgetProps) {
  const [rows, setRows] = useState<ArtistRow[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const num = MAX_ROWS < artistAmount ? MAX_ROWS : artistAmount;

    getTopArtists(num).then((artists) => {
      if (!cancelled) {
        setRows(
          artists.map((artist) => ({
            pic: artist.artworkUrl ? (
              <img className="h-12 w-12 object-cover" src={artist.artworkUrl} alt={artist.name} />
            ) : null,
            artist: artist.name,
            playcount: artist.playCount,
          })),
        );
        setError(null);
      }
    }).catch(() => {
      if (!cancelled) {
        setError('Unable to load top artists.');
      }
    });

    return () => {
      cancelled = true;
    };
  }, [artistAmount]);

  return (
    <section>
      <h2 className='mx-2 my-1 text-xl font-bold'>Top Artists</h2>
      {error ? <p role="alert">{error}</p> : <Table rows={rows} columns={columns} />}
    </section>
  );
}

export default ArtistsWidget;