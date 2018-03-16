import { Music } from "../beans/Music";

const MUSIC_ROOT_PATH = 'audio/';
const LOOP_LENGTH = 4.363900226757369;

export default class MusicLoader {
    context: AudioContext;
    musicPathList: string[];
    onload: Function;
    musics: Music[];
    loadCount: number;

    constructor(context: AudioContext, musicPathList: string[], callback: Function) {
        this.context = context;
        this.musicPathList = musicPathList;
        this.onload = callback;
        this.musics = new Array<Music>();
        this.loadCount = 0;
    }

    loadBuffer(musicPath: string, index: number) {
        // Load buffer asynchronously
        const request = new XMLHttpRequest();
        request.open("GET", MUSIC_ROOT_PATH + musicPath.trim(), true);
        request.responseType = "arraybuffer";

        const _this = this;

        request.onload = () => {
            // Asynchronously decode the audio file data in request.response
            _this.context.decodeAudioData(
                request.response,
                (buffer: AudioBuffer) => {
                    if (!buffer) {
                        console.log('Error decoding file data: ' + musicPath);
                        return;
                    }
                    _this.musics.push(_this._buildMusic(_this._formatMusicName(musicPath), buffer));
                    if (++_this.loadCount === _this.musicPathList.length) {
                        _this.onload(_this.musics);
                    }
                },
                (error) => {
                    console.error('decodeAudioData error', error);
                }
            );
        }
        request.send();
    }

    load() {
        for (let i = 0; i < this.musicPathList.length; ++i)
            this.loadBuffer(this.musicPathList[i], i);
    }

    private _buildMusic(musicName: string, buffer: AudioBuffer): Music {
        const bufferSrc = this.context.createBufferSource()
        bufferSrc.buffer = buffer
        bufferSrc.connect(this.context.destination)
        bufferSrc.loop = true
        bufferSrc.loopStart = 0
        bufferSrc.loopEnd = LOOP_LENGTH
    
        return new Music(musicName, bufferSrc)
      }

    private _formatMusicName(musicPath: string): string {
        return musicPath.split('.')[0];
    }

}