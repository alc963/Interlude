import { Pie, PieChart } from 'recharts';

type MoodData = {
  name: string;
  value: number;
};

type MoodPieChartProps = {
  data: MoodData[];
  isAnimationActive?: boolean;
};

function MoodPieChart({ data, isAnimationActive = true }: MoodPieChartProps) {
  return (
    <PieChart style={{ width: '100%', maxWidth: '500px', maxHeight: '80vh', aspectRatio: 2 }} responsive>
      <Pie
        dataKey="value"
        startAngle={180}
        endAngle={0}
        data={data}
        cx="50%"
        cy="100%"
        outerRadius="120%"
        label
        isAnimationActive={isAnimationActive}
      />
    </PieChart>
  );
}

export default MoodPieChart;