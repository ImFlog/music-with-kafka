
import * as React from 'react';
import { Bar, ChartData } from 'react-chartjs-2';
import MusicChart from '../beans/MusicChart';
import { observer } from 'mobx-react';
import { ChartDataSets, ChartData as ChartDataJs } from 'chart.js';

const defaultDataset: ChartDataSets = {
    label: 'Sounds vote',
    backgroundColor: 'rgba(255,99,132,0.2)',
    borderColor: 'rgba(255,99,132,1)',
    borderWidth: 1,
    hoverBackgroundColor: 'rgba(255,99,132,0.4)',
    hoverBorderColor: 'rgba(255,99,132,1)'
}

export interface MusicChartsProps {
    musicCharts: MusicChart[]
}

@observer
export default class MusicCharts extends React.Component<MusicChartsProps, undefined> {

    buildChartData(): ChartData<ChartDataJs> {
        const sounds: Array<string> = new Array<string>();
        const counts: number[] = [];
        this.props.musicCharts.forEach(musicChart => {
            sounds.push(musicChart.sound);
            counts.push(musicChart.count);
        });

        return { labels: sounds, datasets: [Object.assign({}, defaultDataset, { data: counts })] };
    }

    render() {
        const data = this.buildChartData();
        return (
            <div>
                <h2>Sounds votes for the next music</h2>
                <Bar
                    data={data}
                    width={40}
                    height={10}
                />
            </div>
        );
    }
};