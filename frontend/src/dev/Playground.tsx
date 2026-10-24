import MoodPieChart from "../components/piechart/MoodPieChart";
import TracksWidget from "../components/table/TracksWidget";
import type { Track } from "../util/music";

export function Playground() {
  return (
    <>
      <p>test</p>
      <MoodPieChart
        data={[
          { name: 'Group A', value: 400 },
          { name: 'Group B', value: 300 },
          { name: 'Group C', value: 300 },
          { name: 'Group D', value: 200 },
          { name: 'Group E', value: 278 },
          { name: 'Group F', value: 189 },
        ]}
      />
      {/* <Table
        columns={[
          { key: 'songTitle', label: 'Title' },
          { key: 'artist', label: 'Artist' },
          { key: 'album', label: 'Album' },
        ]}
        rows={[
          { songTitle: 'Midnight City', artist: 'M83', album: "Hurry Up, We're Dreaming" },
          { songTitle: 'Electric Feel', artist: 'MGMT', album: 'Oracular Spectacular' },
          { songTitle: 'Dreams', artist: 'Fleetwood Mac', album: 'Rumours' },
          { songTitle: 'Clocks', artist: 'Coldplay', album: 'A Rush of Blood to the Head' },
          { songTitle: 'Ocean Eyes', artist: 'Billie Eilish', album: 'Dont Smile at Me' },
        ]}
      /> */}
      <TracksWidget trackAmount={4} />
    </>
  );
};

export async function getMockTopTracks(): Promise<Track[]> {
  const rows: Track[] = [
    { name: 'Mycelium', artists: [{ name: 'King Gizzard & The Lizard Wizard' }], album: "Ice, Death, Planets, Lungs, Mushrooms And Lava" },
    {
      name: 'So What',
      artists: [
        { name: 'Miles Davis' },
        { name: 'John Coltrane' },
        { name: 'Cannonball Adderley' },
        { name: 'Bill Evans' },
        { name: 'Paul Chambers' },
        { name: 'Jimmy Cobb' },
      ],
      album: 'Kind of Blue',
    },
    { name: 'My childhood was a period of waiting for the moment when I could send everyone and everything connected with it to hell', artists: [{ name: 'Ciśnienie' }], album: '(angry noises)' },
    { name: 'Dreams', artists: [{ name: 'Fleetwood Mac' }], album: 'Rumours' },
    { name: 'Clocks', artists: [{ name: 'Coldplay' }], album: 'A Rush of Blood to the Head' },
    { name: 'Ocean Eyes', artists: [{ name: 'Billie Eilish' }], album: 'Dont Smile at Me' },
    { name: 'Blinding Lights', artists: [{ name: 'The Weeknd' }], album: 'After Hours' },
    { name: 'Bad Guy', artists: [{ name: 'Billie Eilish' }], album: 'When We All Fall Asleep, Where Do We Go?' },
    { name: 'Take On Me', artists: [{ name: 'a-ha' }], album: 'Hunting High and Low' },
    { name: 'Mr. Brightside', artists: [{ name: 'The Killers' }], album: 'Hot Fuss' },
    { name: 'Royals', artists: [{ name: 'Lorde' }], album: 'Pure Heroine' },
    { name: 'Get Lucky', artists: [{ name: 'Daft Punk' }, { name: 'Pharrell Williams' }], album: 'Random Access Memories' },
    { name: 'Levitating', artists: [{ name: 'Dua Lipa' }], album: 'Future Nostalgia' },
    { name: 'Seven Nation Army', artists: [{ name: 'The White Stripes' }], album: 'Elephant' },
    { name: 'Feel Good Inc.', artists: [{ name: 'Gorillaz' }], album: 'Demon Days' },
    { name: 'Paper Planes', artists: [{ name: 'M.I.A.' }], album: 'Kala' },
  ];
  return rows;
};