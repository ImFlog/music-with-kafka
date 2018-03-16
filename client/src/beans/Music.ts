import { observable } from 'mobx';

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

export enum MUSIC_STATE {
    STOP = 0,
    PLAYING = 1,
    STOPPING = 2
}