import type { TopArtistSummaryResponse } from "../bridge/types/interlude";

type TopArtistsInteractiveProps = {
  data: TopArtistSummaryResponse
};

export default function TopArtistsInteractive({
  data,
}: TopArtistsInteractiveProps) {
  return (
    <div className="p2 grid grid-cols-5">
      {data.map(({ artist, albums }, index) => (
        <div className={'row-' + index + 1}>
          <div className="">
            <span style={{ textAlign: 'center' }}>{index + 1}:</span>
          </div>
          <img
            // style={{ display: 'inline-block' }}
            width={index === 0 ? 160 : 90}
            src={artist.artistArtUrl}
            alt="artist-image" />
          <p>{artist.name}</p>
          <p>{albums.map((album) => album.name).join(', ')}</p>
          {/* add Tracks and Albums widgets */}
        </div>
      ))}
    </div>
  );
};