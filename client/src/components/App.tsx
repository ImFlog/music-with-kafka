import * as React from "react";
import { hot } from "react-hot-loader";
import MusicList from './MusicList';
import MusicPlayer from '../services/MusicPlayer';
import MusicEvent from '../beans/MusicEvent';
import TwitterUserList from './TwitterUserList';
import TwitterUserService from "../services/TwitterUserService";
import TwitterUser from "../beans/TwitterUser";

export interface AppProps { }

const sse = new EventSource('http://localhost:8090/stream');

sse.onopen = () => {
    console.log("SSE oppen");
}

class App extends React.Component<AppProps, undefined> {
    count = 0;

  componentDidMount() {
    sse.onmessage = (event) => {
      const musicEvent: MusicEvent = JSON.parse(event.data);
      MusicPlayer.processMusic(musicEvent)
    }
  }

  private playMusic(evt: React.KeyboardEvent<HTMLInputElement>){
    if (evt.which === 13){
      MusicPlayer.processMusic(new MusicEvent(evt.currentTarget.value.split(",")));
      evt.currentTarget.value = '';
    }
  }

  private addTwitterUser(evt: React.KeyboardEvent<HTMLInputElement>){
    if (evt.which === 13){
      const user = evt.currentTarget.value.split(",");
      TwitterUserService.addUser(new TwitterUser(user[0], user[1], ++this.count))
      evt.currentTarget.value = '';
    }
  }

  render() {
    return (
      <div>
        <h1>Musics</h1>
        Play <input type='text' onKeyPress={this.playMusic.bind(this)} />
        <MusicList musics={MusicPlayer.musics} />
        <br />
        Twitter user <input type='text' onKeyPress={this.addTwitterUser.bind(this)} />
        <TwitterUserList twitterUsers={TwitterUserService.users}/>
      </div>
    );
  }
}

export default hot(module)(App)