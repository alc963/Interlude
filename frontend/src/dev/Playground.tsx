import MoodPieChart from "../components/piechart/MoodPieChart";

function Playground() {
  // Sample data
  const data = [
    { name: 'Group A', value: 400 },
    { name: 'Group B', value: 300 },
    { name: 'Group C', value: 300 },
    { name: 'Group D', value: 200 },
    { name: 'Group E', value: 278 },
    { name: 'Group F', value: 189 },
  ];

  return (
    <>
      <p>test</p>
      <MoodPieChart data={data} />
    </>
  );
}

export default Playground;