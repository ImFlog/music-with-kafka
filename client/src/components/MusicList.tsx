import * as React from "react";
import {observer} from "mobx-react";
import { Music } from '../services/MusicPlayer';

export interface MusicListProps {
  musics: Music[]
}

@observer
export default class MusicList extends React.Component<MusicListProps, undefined> {

  render(){
    const musics = this.props.musics.map((music, idx) => (
      <div key={idx}>{music.name}</div>
    ));
    return (
      <div>
        {musics}
      </div>
    );
  }

}
