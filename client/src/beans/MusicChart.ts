export default class MusicChart {
    sound: string;
    count: number;
  
    constructor(sound: string, count: number) {
      this.sound = sound;
      this.count = count;
    }

    compare(chart: MusicChart): number {
      if (this.count < chart.count) {
        return 1;
      }
      if (this.count > chart.count) {
        return -1;
      }
      return 0;
    }
  }