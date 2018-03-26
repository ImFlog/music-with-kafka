import { observable, IObservableArray } from 'mobx';
import MusicChart from '../beans/MusicChart';

class MusicChartService {
    @observable musicCharts: IObservableArray<MusicChart> = observable([]);

    updateCharts(charts: MusicChart[]) {
        const newCharts = charts.map(chart => new MusicChart(chart.sound, chart.count));

        this.musicCharts.replace(newCharts);
    }
}

export default new MusicChartService