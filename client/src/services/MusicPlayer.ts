import { observable, IObservableArray } from 'mobx';
import MusicEvent from '../beans/MusicEvent';
import { Music, MUSIC_STATE } from '../beans/Music';
import MusicLoader from './MusicLoader';

const MAGIC_NUMBER = 4363.900226757;

class MusicPlayer {
  @observable musics: IObservableArray<Music> = observable([]);
  audioCtx: AudioContext;
  musicLoader: MusicLoader;

  constructor() {
    this.audioCtx = new AudioContext();
    setInterval(this._soundWorker.bind(this), MAGIC_NUMBER);
  }

  processMusic(musicEvent: MusicEvent) {
    console.log("Received event", musicEvent)

    // STOP the music being played
    this._stopPlayingMusic();

    this.musicLoader = new MusicLoader(
      this.audioCtx,
      musicEvent.sounds,
      this._playMusic.bind(this)
    );

    this.musicLoader.load()
  }

  private _stopPlayingMusic() {
    this.musics.forEach((music) => {
      music.state = MUSIC_STATE.STOPPING;
    });
  }

  private _playMusic(musics: Music[]) {
    const _this = this;
    musics.forEach((music) => {
      _this.musics.push(music);
    })
  }

  private _soundWorker() {
    this.musics.forEach(music => {
      if (music.state === MUSIC_STATE.STOPPING) {
        music.buffer.stop();
      } else if (music.state !== MUSIC_STATE.PLAYING) {
        music.buffer.start();
        music.state = MUSIC_STATE.PLAYING;
      }
    });
    this._removedStoppedMusic();
  }

  private _removedStoppedMusic() {
    const newMusics = this.musics.filter(music => {
      return music.state === MUSIC_STATE.PLAYING
    })

    this.musics.replace(newMusics);
  }
}

export default new MusicPlayer;
