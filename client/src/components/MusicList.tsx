import * as React from "react";
import {observer} from "mobx-react";
import { Music, MUSIC_STATE } from '../beans/Music';

export interface MusicListProps {
  musics: Music[]
}

@observer
export default class MusicList extends React.Component<MusicListProps, undefined> {

  render(){
    const musics = this.props.musics.filter((music) => music.state !== MUSIC_STATE.STOP).map((music, idx) => (
      <div key={idx}>{music.name}</div>
    ));
    return (
      <div>
        {musics}
      </div>
    );
  }

}
