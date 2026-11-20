import { useEffect, useState } from 'react';
import { ApiBridge } from '../../bridge/api_bridge';
import type { Album } from '../../bridge/types/music';
import Table from './Table';

type AlbumsWidgetProps = {
  albumAmount?: number;
};

type AlbumRow = {
  pic: string;
  album: string;
  artist: string;
  playcount: number;
};

type TopAlbum = Album & {
  playCount?: number;
};

const columns = [
  { key: 'pic', label: '' },
  { key: 'album', label: 'Album' },
  { key: 'artist', label: 'Artists' },
  { key: 'playcount', label: 'Plays' },
];

const MAX_ROWS = 10;

async function getTopAlbums(): Promise<TopAlbum[]> {
  const albums = await ApiBridge.get<TopAlbum[]>('/me/top/albums');
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

    getTopAlbums().then((albums: TopAlbum[]) => {
      if (!cancelled) {
        setRows(
          albums.slice(0, num).map((album) => ({
            pic: album.albumArtUrl ?? '',
            album: album.name,
            artist: album.artists.map((artist) => artist.name).join(', '),
            playcount: album.playCount ?? 0,
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
