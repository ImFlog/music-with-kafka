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
import * as NotificationSystem from "react-notification-system";
import UserMessage from "../beans/UserMessage";
import { withStyles } from 'material-ui/styles';
import AppBar from 'material-ui/AppBar';
import Toolbar from 'material-ui/Toolbar';
import Typography from 'material-ui/Typography';
import Card, { CardContent } from 'material-ui/Card';
import Grid from 'material-ui/Grid';

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
const sseUserMessage = new EventSource('http://localhost:8090/stream/user-message');

export interface AppProps { }

class App extends React.Component<AppProps, undefined> {
  count = 0;
  _notificationSystem: NotificationSystem.System;

  componentDidMount() {
    // Only for testing
    // MusicChartService.updateCharts(musicCharts);
    // testUsers.forEach(user => TwitterUserService.addUser(user));

    sseSounds.onmessage = (event) => {
      const musicEvent: MusicEvent = JSON.parse(event.data);
      MusicPlayer.processMusic(musicEvent);
      // MusicChartService.cleanCharts();
    }

    sseUsers.onmessage = (event) => {
      const user: TwitterUser = JSON.parse(event.data);
      TwitterUserService.addUser(user);
    }

    sseCharts.onmessage = (event) => {
      const musicChartEvent: MusicChartEvent = JSON.parse(event.data);
      MusicChartService.updateCharts(musicChartEvent.charts);
    }

    sseUserMessage.onmessage = (event) => {
      const userMessage: UserMessage = JSON.parse(event.data);
      this.addNotification(userMessage);
    }
  }

  private addNotification(message: UserMessage) {
    if (this._notificationSystem.state.notifications.length >= 5) {
      this._notificationSystem.state.notifications.shift()
    }

    this._notificationSystem.addNotification({
      title: message.name,
      message: message.message,
      level: 'info',
      dismissible: false
    })
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
      <div style={styles.root}>
      <AppBar position="static" color="primary">
        <Toolbar>
          <Typography variant="title" color="inherit">
            Music w/ Kafka
          </Typography>
        </Toolbar>
      </AppBar>


      <Grid style={styles.container} container spacing={24}>
        <Grid item xs={12} sm={4}>
            <Card style={styles.tweetStyle}>
              <CardContent>
                <Typography variant="headline" component="h1">
                  Top Twittos
                </Typography>
                <TwitterUserList twitterUsers={TwitterUserService.users} />
              </CardContent>
            </Card>
        </Grid>

        <Grid item xs={12} sm={8}>
          <Grid container spacing={24}>
            <Grid item sm={11}>
              <MusicList musics={MusicPlayer.musics} incomingMusics={MusicPlayer.incomingMusics} />
            </Grid>
          </Grid>
          <Grid container spacing={24}>
            <Grid item sm={11}>
              <MusicCharts musicCharts={MusicChartService.musicCharts} />
            </Grid>
          </Grid>
        </Grid>

      </Grid>


      <Grid style={styles.container} container spacing={24}>
        <Grid item sm={4}></Grid>
        <Grid item sm={7}>
          <Card>
            <CardContent>
              <Typography variant="headline" component="h1">
                Available Categories
              </Typography>
              <h2>drum  |  heavy_bass  |  lead_bass  |  line_bass  |  melody  |  pad  |  synth  |  vocal</h2>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <NotificationSystem ref={(ref: any) => this._notificationSystem = ref} />
  
      </div>
    );
  }
}

const styles = {
  root: {
    flexGrow: 1
  },
  container: {
    margin: '10px'
  },
  appStyle: {
    width: '90%',
    margin: 'auto',
    padding: '10px'
  },
  tweetStyle: {
    height: '600px'
  },
  musicStyle: {
    marginLeft: '40%'
  },
  chartStyle: {
    marginTop: '13%',
    marginLeft: '40%'
  },
  card: {
    minWidth: 275
  }
}

export default hot(module)(App)