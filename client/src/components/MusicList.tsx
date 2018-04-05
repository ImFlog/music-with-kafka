import * as React from "react";
import { observer } from "mobx-react";
import { Music, MUSIC_STATE } from '../beans/Music';

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
        <div style={styles.inProgressStyle}>
          <h1>Music in progress</h1>
          <p style={styles.musicStyle}>{musics}</p>
        </div>
        <div style={styles.incomingStyle}>
          <h1>Next Music</h1>
          <p style={styles.musicStyle}>{incomingMusics}</p>
        </div>
      </div>
    );
  }

}

const styles = {
  inProgressStyle: {
    width: '45%',
    float: 'left'
  },
  incomingStyle: {
    marginLeft: '5%',
    float: 'left',
    width: '45%'
  },
  musicStyle: {
    borderLeft: '6px solid #005580',
    backgroundColor: '#e6f7ff',
    width: '90%'
  }
}
