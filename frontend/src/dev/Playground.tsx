import MoodPieChart from "../components/piechart/MoodPieChart";
// import Table from "../components/table/Table";
import TracksWidget from "../components/table/TracksWidget";

function Playground() {
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
}

export default Playground;