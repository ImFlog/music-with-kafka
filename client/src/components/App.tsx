import * as React from "react";
import { hot } from "react-hot-loader";
import MusicList from './MusicList';
import MusicPlayer from '../services/MusicPlayer';
import MusicEvent from '../beans/MusicEvent';

export interface AppProps { }

const sse = new EventSource('http://localhost:8090/stream');

sse.onopen = () => {
    console.log("SSE oppen");
}

class App extends React.Component<AppProps, undefined> {

  componentDidMount() {
    sse.onmessage = (event) => {
      const musicEvent: MusicEvent = JSON.parse(event.data);
      MusicPlayer.processMusic(musicEvent)
    }
  }

  private playMusic(evt: React.KeyboardEvent<HTMLInputElement>){
    if (evt.which === 13){
      MusicPlayer.processMusic(new MusicEvent(evt.currentTarget.value, "PLAY"));
      evt.currentTarget.value = '';
    }
  }

  private stopMusic(evt: React.KeyboardEvent<HTMLInputElement>){
    if (evt.which === 13){
      MusicPlayer.processMusic(new MusicEvent(evt.currentTarget.value, "STOP"));
      evt.currentTarget.value = '';
    }
  }

  render() {
    return (
      <div>
        <h1>Musics</h1>
        Play <input type='text' onKeyPress={this.playMusic.bind(this)} />
        Stop <input type='text' onKeyPress={this.stopMusic.bind(this)} />
        <MusicList musics={MusicPlayer.musics} />
      </div>
    );
  }
}

export default hot(module)(App)