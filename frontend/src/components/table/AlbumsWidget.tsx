import { useEffect, useState, type ReactNode } from 'react';
import { ApiBridge } from '../../bridge/api_bridge';
import type { TopAlbumsResponse } from '../../bridge/types/interlude';
import Table from './Table';

type AlbumsWidgetProps = {
  albumAmount?: number;
};

type AlbumRow = {
  pic: ReactNode;
  album: string;
  artist: string;
  playcount: number;
};

const columns = [
  { key: 'pic', label: '' },
  { key: 'album', label: 'Album' },
  { key: 'artist', label: 'Artists' },
  { key: 'playcount', label: 'Plays' },
];

const MAX_ROWS = 10;

async function getTopAlbums(limit: number): Promise<TopAlbumsResponse> {
  const albums = await ApiBridge.top_albums({ limit });
  if (albums instanceof Response) {
    throw new Error(`Failed to load top albums (${albums.status})`);
  }
  return albums;
}

function AlbumsWidget({ albumAmount = 5 }: AlbumsWidgetProps) {
  const [rows, setRows] = useState<AlbumRow[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const num = MAX_ROWS < albumAmount ? MAX_ROWS : albumAmount;

    getTopAlbums(num).then((albums) => {
      if (!cancelled) {
        setRows(
          albums.map((album) => ({
            pic: album.artworkUrl ? (
              <img className="h-12 w-12 object-cover" src={album.artworkUrl} alt={`${album.name} cover`} />
            ) : null,
            album: album.name,
            artist: album.artists.map((artist) => artist.name).join(', '),
            playcount: album.playCount,
          })),
        );
        setError(null);
      }
    }).catch(() => {
      if (!cancelled) {
        setError('Unable to load top albums.');
      }
    });

    return () => {
      cancelled = true;
    };
  }, [albumAmount]);

  return (
    <section>
      <h2 className='mx-2 my-1 text-xl font-bold'>Top Albums</h2>
      {error ? <p role="alert">{error}</p> : <Table rows={rows} columns={columns} />}
    </section>
  );
}

export default AlbumsWidget;
