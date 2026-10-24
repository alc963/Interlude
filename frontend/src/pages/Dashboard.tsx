import TopTracksWidget from '../components/table/TracksWidget';

function Dashboard() {
  return (
    <main>
      <h1 className="mx-5 my-5 text-3xl font-bold">Welcome back!</h1>
      <div style={{ paddingLeft: '10%', paddingRight: '10%' }}>
        <TopTracksWidget />
      </div>
    </main>
  );
}

export default Dashboard;
