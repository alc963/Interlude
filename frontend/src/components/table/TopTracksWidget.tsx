import { useEffect, useState } from 'react';
import { getTopTracks } from '../../services/api';
import type { Track } from '../../util/types';
import Table from './Table';

type TopTracksWidgetProps = {
  trackAmount?: number;
};

type TopTrackRow = {
  title: string;
  artist: string;
  album: string;
};

const columns = [
  { key: 'title', label: 'Title' },
  { key: 'artist', label: 'Artist(s)' },
  { key: 'album', label: 'Album' },
];

const MAX_ROWS = 10;

function TopTracksWidget({ trackAmount = 5 }: TopTracksWidgetProps) {
  const [rows, setRows] = useState<TopTrackRow[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    getTopTracks(trackAmount).then((tracks: Track[]) => {
      if (!cancelled) {
        setRows(
          tracks.slice(0, MAX_ROWS).map((track) => ({
            title: track.name,
            artist: track.artists.map((artist) => artist.name).join(', '),
            album: track.album,
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
      <h2>Top Tracks</h2>
      {error ? <p role="alert">{error}</p> : <Table rows={rows} columns={columns} />}
    </section>
  );
}

export default TopTracksWidget;