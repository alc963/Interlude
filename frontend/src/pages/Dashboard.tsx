import TopTracksWidget from '../components/table/TracksWidget';
import MoodPieChart from '../components/piechart/MusicPieChart';

const data = [
  { name: 'Indie Rock', value: 32 },
  { name: 'Hip-Hop', value: 24 },
  { name: 'Electronic', value: 19 },
  { name: 'Jazz', value: 14 },
  { name: 'Pop', value: 11 },
];

function Dashboard() {
  return (
    <main>
      <h1 className="mx-5 my-5 text-3xl font-bold">Welcome back!</h1>
      <div style={{ paddingLeft: '10%', paddingRight: '10%' }}>
        <TopTracksWidget />
        <MoodPieChart data={data} chartType='genre'/>
      </div>
    </main>
  );
}

export default Dashboard;
