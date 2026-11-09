import { Pie, PieChart, Tooltip, type TooltipIndex } from 'recharts';
import { getGenreColor } from '../../bridge/types/music';

type ChartData = {
  name: string;
  value: number;
};

type MusicPieChartProps = {
  data: ChartData[];
  chartType: 'mood' | 'genre';
  isAnimationActive?: boolean;
  defaultIndex?: TooltipIndex;
};

export default function MusicPieChart({
  data,
  chartType,
  isAnimationActive = true,
  defaultIndex
}: MusicPieChartProps) {

  const coloredData = chartType === 'genre' ?
    data.map((items) => ({ ...items, fill: getGenreColor(items.name) })) :
    data.map((items) => ({ ...items, fill: '#FFFFFF' }));

  return (
    <PieChart style={{ width: '100%', maxWidth: '500px', maxHeight: '80vh', aspectRatio: 2 }} responsive>
      <Pie
        dataKey="value"
        nameKey="name"
        startAngle={180}
        endAngle={0}
        data={coloredData}
        cx="50%"
        cy="100%"
        outerRadius="120%"
        isAnimationActive={isAnimationActive}
      />
      <Tooltip defaultIndex={defaultIndex} />
    </PieChart>
  );
}