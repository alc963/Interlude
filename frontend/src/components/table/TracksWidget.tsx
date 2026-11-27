import { useEffect, useState } from 'react';
import { ApiBridge } from '../../bridge/api_bridge';
import type { TopTrack } from '../../bridge/types/interlude';
import Table from './Table';

type TracksWidgetProps = {
  trackAmount?: number;
};

type TrackRow = {
  rank: number;
  title: string;
  artist: string;
  album: string;
  playcount: number;
};

const columns = [
  { key: 'rank', label: '' },
  { key: 'title', label: 'Title' },
  { key: 'artist', label: 'Artist(s)' },
  { key: 'album', label: 'Album' },
  { key: 'playcount', label: 'Plays' },
];

const MAX_ROWS = 10;

async function getTopTracks(limit: number): Promise<TopTrack[]> {
  const tracks = await ApiBridge.top_tracks({ limit });
  if (tracks instanceof Response) {
    throw new Error(`Failed to load top tracks (${tracks.status})`);
  }
  return tracks;
}

function TracksWidget({ trackAmount = 5 }: TracksWidgetProps) {
  const [rows, setRows] = useState<TrackRow[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const num = MAX_ROWS < trackAmount ? MAX_ROWS : trackAmount;

    getTopTracks(num).then((tracks: TopTrack[]) => {
      if (!cancelled) {
        setRows(
          tracks.map((track) => ({
            rank: track.rank,
            title: track.name,
            artist: track.artists.map((artist) => artist.name).join(', '),
            album: track.album ?? '',
            playcount: track.playCount,
          })),
        );
        setError(null);
      }
    }).catch(() => {
      if (!cancelled) {
        setError('Unable to load top tracks.');
      }
    });

    return () => {
      cancelled = true;
    };
  }, [trackAmount]);

  return (
    <section>
      <h2 className='mx-2 my-1 text-xl font-bold'>Top Tracks</h2>
      {error ? <p role="alert">{error}</p> : <Table rows={rows} columns={columns} />}
    </section>
  );
}

export default TracksWidget;