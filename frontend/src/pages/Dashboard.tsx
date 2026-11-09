// import TopTracksWidget from '../components/table/TracksWidget';
import MusicPieChart from '../components/piechart/MusicPieChart';
import TopArtistsInteractive from '../components/TopArtistsInteractive';
import useTopArtistsSummary from '../hooks/useTopArtistsSummary';

const data = [
  { name: 'Indie Rock', value: 32 },
  { name: 'Hip-Hop', value: 24 },
  { name: 'Electronic', value: 19 },
  { name: 'Jazz', value: 14 },
  { name: 'Pop', value: 11 },
  { name: 'foobar', value: 2 },
];

function Dashboard() {
  const { topArtistsSummary, loading, error } = useTopArtistsSummary();

  return (
    <main>
      <h1 className="mx-5 my-5 text-3xl font-bold">Welcome back!</h1>
      <div style={{ paddingLeft: '10%', paddingRight: '10%' }}>
        {topArtistsSummary ? (<TopArtistsInteractive data={topArtistsSummary} />) :
          loading ? (<p>Loading top artists...</p>)
            : error ? (<p role="alert">{error}</p>)
              : null}
        {/* <TopTracksWidget /> */}
        <MusicPieChart data={data} chartType='genre' />
      </div>
    </main>
  );
}

export default Dashboard;
