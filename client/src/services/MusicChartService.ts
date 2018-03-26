import { observable, IObservableArray } from 'mobx';
import MusicChart from '../beans/MusicChart';
import MusicChartEvent from '../beans/MusicChartEvent';

const sse = new EventSource('http://localhost:8090/stream/charts');

class MusicChartService {
    @observable musicCharts: IObservableArray<MusicChart> = observable([]);

    constructor() {
        sse.onmessage = (event) => {
            const musicChartEvent: MusicChartEvent = JSON.parse(event.data);
            this.updateCharts(musicChartEvent.charts);
        }
    }

    updateCharts(charts: MusicChart[]) {
        this.musicCharts.replace(charts);
    }
}

export default new MusicChartService