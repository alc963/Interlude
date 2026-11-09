import { useEffect, useState } from 'react';
import { ApiBridge } from '../../bridge/api_bridge';
import type { Artist } from '../../bridge/types/music';
import Table from './Table';

type ArtistsWidgetProps = {
  artistAmount?: number;
};

type ArtistRow = {
  pic: string;
  artist: string;
  playcount: number;
};

type TopArtist = Artist & {
  playCount?: number;
};

const columns = [
  { key: 'pic', label: '' },
  { key: 'artist', label: 'Artists' },
  { key: 'playcount', label: 'Plays' },
];

const MAX_ROWS = 10;

async function getTopArtists(): Promise<TopArtist[]> {
  const artists = await ApiBridge.get<TopArtist[]>('/me/top/artists');
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

    getTopArtists().then((artists: TopArtist[]) => {
      if (!cancelled) {
        setRows(
          artists.slice(0, num).map((artist) => ({
            pic: artist.artistArtUrl ?? '',
            artist: artist.name,
            playcount: artist.playCount ?? 0,
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