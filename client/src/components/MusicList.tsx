import * as React from "react";
import { observer } from "mobx-react";
import { Music, MUSIC_STATE } from '../beans/Music';
import Card, { CardContent } from 'material-ui/Card';
import Typography from 'material-ui/Typography';
import Grid from 'material-ui/Grid';

export interface MusicListProps {
  musics: Music[]
  incomingMusics: String[]
}

@observer
export default class MusicList extends React.Component<MusicListProps, undefined> {

  render() {
    const musics = this.props.musics.filter((music) => music.state !== MUSIC_STATE.STOP).map((music, idx) => (
      <div key={music.name + idx}>{music.name}</div>
    ));
    const incomingMusics = this.props.incomingMusics.map((music, idx) => (
      <div key={music + " " + idx}>{music}</div>
    ));
    return (
      <div>
        <Grid container spacing={24}>
          <Grid item sm={6}>
            <Card style={styles.container}>
              <CardContent>
                <Typography variant="headline" component="h1">
                  Music in progress
                </Typography>
                <Typography style={styles.musicStyle} color="textSecondary">
                  {musics}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item sm={6}>
            <Card style={styles.container}>
              <CardContent>
                <Typography variant="headline" component="h1">
                  Next Music
                </Typography>
                <Typography style={styles.musicStyle} color="textSecondary">
                  {incomingMusics}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </div>
    );
  }

}

const styles = {
  musicStyle: {
    fontSize: '19px'
  },
  container: {
    height: '250px'
  }
}
