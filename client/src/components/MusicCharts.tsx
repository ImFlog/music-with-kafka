
import * as React from 'react';
import { Bar, ChartData } from 'react-chartjs-2';
import MusicChart from '../beans/MusicChart';
import { observer } from 'mobx-react';
import { ChartDataSets, ChartData as ChartDataJs, ChartColor, ChartOptions } from 'chart.js';
import Paper from 'material-ui/Paper';
import Typography from 'material-ui/Typography';
import Card, { CardContent } from 'material-ui/Card';

const defaultColor: ChartColor = 'rgba(255,99,132,0.2)'
const defaultBorderColor: ChartColor = 'rgba(255,99,132,1)'
const topColor: ChartColor = 'rgba(145,222,114,1)'
const topBorderColor: ChartColor = 'rgba(105,187,72,1)'

const defaultDataset: ChartDataSets = {
    label: 'Sounds vote',
    borderWidth: 1
}

const options: ChartOptions = {
    scales: {
        yAxes: [{
            ticks: {
                min: 0,
                max: 10,
            }
        }]
    }
}

export interface MusicChartsProps {
    musicCharts: MusicChart[]
}

@observer
export default class MusicCharts extends React.Component<MusicChartsProps, undefined> {

    buildChartData(): ChartData<ChartDataJs> {
        const sounds: Array<string> = new Array<string>();
        const counts: number[] = new Array<number>();
        const backgroundColors: ChartColor[] = new Array<ChartColor>();
        const borderColors: ChartColor[] = new Array<ChartColor>();
        this.props.musicCharts.sort((music1, music2) => music1.compare(music2)).forEach((musicChart, idx) => {
            if (idx < 5) {
                backgroundColors.push(topColor);
                borderColors.push(topBorderColor);
            } else {
                backgroundColors.push(defaultColor);
                borderColors.push(defaultBorderColor);
            }
            sounds.push(musicChart.sound);
            counts.push(musicChart.count);
        });

        const dataset: ChartDataSets = Object.assign({},
            defaultDataset,
            { data: counts },
            { backgroundColor: backgroundColors },
            { borderColor: borderColors }
        )

        return { labels: sounds, datasets: [dataset] };
    }

    render() {
        const data = this.buildChartData();
        return (
            <div>
                <Card>
                    <CardContent>
                        <Typography variant="headline" component="h1">
                            Sounds votes for the next music
                        </Typography>
                        <Bar
                            data={data}
                            width={40}
                            height={10}
                            options={options}
                        />
                    </CardContent>
                </Card>
            </div>
        );
    }
};