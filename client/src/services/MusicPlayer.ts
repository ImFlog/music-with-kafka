import { observable, IObservableArray } from 'mobx';
import MusicEvent from '../beans/MusicEvent';

const MAGIC_NUMBER = 4363.900226757;
const LOOP_LENGTH = 4.363900226757369;
enum MUSIC_STATE {
  STOP = 0,
  PLAYING = 1,
  STOPPING = 2
}

export class Music {
  @observable name: string;
  buffer: AudioBufferSourceNode;
  state: MUSIC_STATE;

  constructor(name: string, buffer: AudioBufferSourceNode) {
    this.name = name;
    this.state = MUSIC_STATE.STOP;
    this.buffer = buffer;
  }
}

class MusicPlayer {
  @observable musics: IObservableArray<Music> = observable([]);
  audioCtx: AudioContext;

  constructor() {
    this.audioCtx = new AudioContext();
    setInterval(this._soundWorker.bind(this), MAGIC_NUMBER);
  }

  processMusic(musicEvent: MusicEvent) {
    console.log("Received event", musicEvent)
    if (musicEvent.action === 'PLAY') {
      this._addToPlaylist(musicEvent.name)
    } else if (musicEvent.action === 'STOP') {
      let music = this.musics.find((music) => {
        return music.name === musicEvent.name
      });
      music.state = MUSIC_STATE.STOPPING;
    }
  }

  private _soundWorker(){
    this.musics.forEach(music => {
      if (music.state === 2){
        music.buffer.stop();
      } else if (music.state !== 1){
        music.buffer.start();
        music.state = MUSIC_STATE.PLAYING;
      }
    });
    this._removedStoppedMusic();
  }

  private _removedStoppedMusic(){
    const newMusics = this.musics.filter(music => {
      return music.state === 1
    })

    this.musics.replace(newMusics);
  }

  private _addToPlaylist(musicName: string) {
    this._loadSound(musicName);
  }

  private _loadSound(musicName: string) {
    const request = new XMLHttpRequest();
    const musicPath = 'audio/' + musicName + '.ogg';
    request.open('GET', musicPath, true);
    request.responseType = 'arraybuffer';

    const _this = this;

    // Decode asynchronously
    request.onload = function () {
      _this.audioCtx.decodeAudioData(request.response, (buffer: AudioBuffer) => {
        _this.musics.push(_this._buildMusic(musicName, buffer));
      });
    }
    request.send();
  }

  private _buildMusic(musicName: string, buffer: AudioBuffer): Music {
    const bufferSrc = this.audioCtx.createBufferSource()
    bufferSrc.buffer = buffer
    bufferSrc.connect(this.audioCtx.destination)
    bufferSrc.loop = true
    bufferSrc.loopStart = 0
    bufferSrc.loopEnd = LOOP_LENGTH

    return new Music(musicName, bufferSrc)
  }

}

export default new MusicPlayer;
