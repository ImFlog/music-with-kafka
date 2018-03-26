import * as React from "react";
import { hot } from "react-hot-loader";
import MusicList from './MusicList';
import MusicPlayer from '../services/MusicPlayer';
import MusicEvent from '../beans/MusicEvent';
import TwitterUserList from './TwitterUserList';
import TwitterUserService from "../services/TwitterUserService";
import TwitterUser from "../beans/TwitterUser";
import MusicCharts from "./MusicCharts";
import MusicChart from "../beans/MusicChart";
import MusicChartService from "../services/MusicChartService";
import MusicChartEvent from "../beans/MusicChartEvent";

const musicCharts: MusicChart[] = [
  new MusicChart("sound1", 12),
  new MusicChart("bass4", 5),
  new MusicChart("drum5", 8),
  new MusicChart("heavy12", 20),
  new MusicChart("voice5", 10)
];

const testUsers: TwitterUser[] = [
  new TwitterUser("monty", "https://static.boredpanda.com/blog/wp-content/uploads/2014/11/most-popular-cats-monty-2.jpg", 12),
  new TwitterUser("honey_bee", "https://static.boredpanda.com/blog/wp-content/uploads/2014/11/most-popular-cats-honey-bee__605.jpg", 3),
  new TwitterUser("venus", "https://static.boredpanda.com/blog/wp-content/uploads/2014/11/most-popular-cats-venus-21.jpg", 5),
  new TwitterUser("lil_bub", "https://static.boredpanda.com/blog/wp-content/uploads/2014/11/most-popular-cats-lil-bub-11__605.jpg", 2),
  new TwitterUser("maru", "https://static.boredpanda.com/blog/wp-content/uploads/2014/11/famous-internet-cats-201__605.jpg", 23),
  new TwitterUser("grumpycat", "https://static.boredpanda.com/blog/wp-content/uploads/2014/11/most-popular-cats-grumpy-cat-11__605.jpg", 40),
  new TwitterUser("garfy", "https://static.boredpanda.com/blog/wp-content/uploads/2014/11/garfi-evil-grumpy-persian-cat-12.jpg", 1)
]

const sseSounds = new EventSource('http://localhost:8090/stream/sounds');
const sseUsers = new EventSource('http://localhost:8090/stream/users');
const sseCharts = new EventSource('http://localhost:8090/stream/charts');

export interface AppProps { }

class App extends React.Component<AppProps, undefined> {
  count = 0;

  componentDidMount() {
    // Only for testing
    // MusicChartService.updateCharts(musicCharts);
    // testUsers.forEach(user => TwitterUserService.addUser(user));

    sseSounds.onmessage = (event) => {
      const musicEvent: MusicEvent = JSON.parse(event.data);
      MusicPlayer.processMusic(musicEvent)
    }

    sseUsers.onmessage = (event) => {
      const user: TwitterUser = JSON.parse(event.data);
      TwitterUserService.addUser(user);
    }

    sseCharts.onmessage = (event) => {
      const musicChartEvent: MusicChartEvent = JSON.parse(event.data);
      MusicChartService.updateCharts(musicChartEvent.charts);
    }
  }

  private playMusic(evt: React.KeyboardEvent<HTMLInputElement>) {
    if (evt.which === 13) {
      MusicPlayer.processMusic(new MusicEvent(evt.currentTarget.value.split(",")));
      evt.currentTarget.value = '';
    }
  }

  private addTwitterUser(evt: React.KeyboardEvent<HTMLInputElement>) {
    if (evt.which === 13) {
      const user = evt.currentTarget.value.split(",");
      TwitterUserService.addUser(new TwitterUser(user[0], user[1], ++this.count))
      evt.currentTarget.value = '';
    }
  }

  render() {
    return (
      <div style={styles.appStyle}>
        <div style={styles.tweetStyle}>
          <h1>Top Twittos </h1> <input type='text' onKeyPress={this.addTwitterUser.bind(this)} />
          <TwitterUserList twitterUsers={TwitterUserService.users} />
        </div>

        <div style={styles.musicStyle}>
          <h1>Music in progress</h1> <input type='text' onKeyPress={this.playMusic.bind(this)} />
          <MusicList musics={MusicPlayer.musics} />
        </div>

        <div style={styles.chartStyle}>
          <MusicCharts musicCharts={MusicChartService.musicCharts} />
        </div>
      </div>
    );
  }
}

const styles = {
  appStyle: {
    width: '90%',
    margin: 'auto',
    padding: '10px'
  },
  tweetStyle: {
    width: '15%',
    float: 'left'
  },
  musicStyle: {
    marginLeft: '40%'
  },
  chartStyle: {
    marginTop: '13%',
    marginLeft: '40%'
  }
}

export default hot(module)(App)