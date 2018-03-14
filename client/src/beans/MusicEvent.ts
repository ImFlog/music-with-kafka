export default class MusicEvent {
    path: string;
    action: string;
  
    constructor(path: string, action: string) {
      this.path = path;
      this.action = action;
    }
  }