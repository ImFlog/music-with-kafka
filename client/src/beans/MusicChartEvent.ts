import MusicChart from "./MusicChart";

export default class MusicChartEvent {
    charts: MusicChart[];
  
    constructor(charts: MusicChart[]) {
      this.charts = charts;
    }
  }