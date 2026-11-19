import { useState } from "react";
import type { TopArtistSummaryResponse } from "../bridge/types/interlude";
import Table from "./table/Table";

const albumColumns = [
  { key: "rank", label: "" },
  { key: "art", label: "" },
  { key: "name", label: "Album" },
  { key: "playCount", label: "Play Count" },
];

const trackColumns = [
  { key: "rank", label: "" },
  { key: "name", label: "Track" },
  { key: "playCount", label: "Play Count" },
];

type TopArtistsInteractiveProps = {
  data: TopArtistSummaryResponse
};

export default function TopArtistsInteractive({ data }: TopArtistsInteractiveProps) {
  const [selected_index, set_selected_index] = useState(0);
  const selectedArtist = data[selected_index];

  const albumRows = selectedArtist?.albums.map((album, index) => ({
    rank: index + 1,
    art: album.albumArtUrl ? (
      <img className="h-20 w-20 object-cover" src={album.albumArtUrl} alt={`${album.name} cover`} />
    ) : null,
    name: album.name,
    playCount: album.playCount ?? 0,
  })) ?? [];
  const trackRows = selectedArtist?.tracks.map((track, index) => ({
    rank: index + 1,
    name: track.name,
    playCount: track.playCount ?? 0,
  })) ?? [];

  return (
    <div
      style={{ border: '3px #45556c dotted', borderRight: 'none', borderLeft: 'none' }}
      className="p2 grid grid-cols-[max-content_minmax(0,1fr)]">
      <div>
        {data.map(({ artist }, index) => (
          <div className="p-3 grid grid-cols-[min-content_150px_200px] gap-3 cursor-pointer" onClick={() => set_selected_index(index)}>
            <div className="relative flex items-center justify-center after:absolute after:right-[-6px] after:inset-y-0 after:w-px after:bg-slate-600 after:content-['']">
              <span className="text-2xl p-5">{index + 1}</span>
            </div>
            <div className="flex items-center justify-center" style={{ padding: '6px' }}>
              <span className="text-lg">{artist.name}</span>
            </div>
            <div>
              <img
                className={`max-w-none transition-[width] duration-300 ease-in-out motion-reduce:transition-none ${index === selected_index ? "w-[200px]" : "w-[90px]"}`}
                src={artist.artistArtUrl}
                alt="artist-image" />
            </div>
          </div>
        ))}
      </div>
      <div className="p-5 min-w-0 ">
        <section className="mb-8">
          <Table rows={albumRows} columns={albumColumns} verticalAlign="middle" />
        </section>
        <section>
          <Table rows={trackRows} columns={trackColumns} verticalAlign="middle" />
        </section>
      </div>
    </div>
  );
};