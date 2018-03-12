const audioCtx = new AudioContext();
const timerWorker = new Worker('../js/worker.js');
const MAGIC_NUMBER = 4363.900226757;
const LOOP_LENGTH = 4.363900226757369;

const audioMap$ = new Rx.BehaviorSubject(new Map());
const runningSet$ = new Rx.BehaviorSubject(new Set());
const music$ = new Rx.Subject();
const processPlay$ = new Rx.Subject();
const processStop$ = new Rx.Subject();
const release$ = new Rx.Subject();

//Mapping of our sounds

const soundsDefinition = {
    melodies: {
        path: (index) => `../audio/sounds/melodies/melody${index + 1}.ogg`,
        name: (index) => `melody${index + 1}`,
        size: 4
    },
    pads: {
        path: (index) => `../audio/sounds/pads/pad${index + 1}.ogg`,
        name: (index) => `pad${index + 1}`,
        size: 2
    },
    synths: {
        path: (index) => `../audio/sounds/synths/synth${index + 1}.ogg`,
        name: (index) => `synth${index + 1}`,
        size: 4
    },
    vocals: {
        path: (index) => `../audio/sounds/vocals/vocal${index + 1}.ogg`,
        name: (index) => `vocal${index + 1}`,
        size: 6
    }  
};

const drumsDefinition = {
    drum: {
        path: (index) => `../audio/drums/drum${index + 1}.ogg`,
        name: (index) => `drum${index + 1}`,
        size: 10
    }
};

const bassDefinition = {
    heavy: {
        path: (index) => `../audio/bass/heavy/heavy${index + 1}.ogg`,
        name: (index) => `heavy${index + 1}`,
        size: 4
    },
    lead: {
        path: (index) => `../audio/bass/lead/lead${index + 1}.ogg`,
        name: (index) => `lead${index + 1}`,
        size: 2
    },
    line: {
        path: (index) => `../audio/bass/line/line${index + 1}.ogg`,
        name: (index) => `line${index + 1}`,
        size: 4
    }
}


function fetchFile(mapping) {

    const mappings = Object
        .keys(mapping)
        .map(key => {
            const musicDefinition = mapping[key];
            return [...Array(musicDefinition.size).keys()].map(index => [musicDefinition.path(index), musicDefinition.name(index)])
        });

    return mappings
        .map(audioDef => {
            return Rx
                .Observable
                .from(audioDef)
                .mergeMap(([file, name]) => Rx.Observable.of({ name, fetch: loadAudioData(fetch(file)) }));
        });
}

function loadAudioData(promise) {
    return Rx
        .Observable
        .fromPromise(promise)
        .mergeMap(response => Rx.Observable.fromPromise(response.arrayBuffer()))
        .mergeMap(buffer => Rx.Observable.fromPromise(audioCtx.decodeAudioData(buffer)))
}

function createAudioSource(ctx, data) {
    const bufferSrc = ctx.createBufferSource()
    bufferSrc.buffer = data
    bufferSrc.connect(ctx.destination)
    bufferSrc.loop = true
    bufferSrc.loopStart = 0
    bufferSrc.loopEnd = LOOP_LENGTH
    return bufferSrc
}


timerWorker.onmessage = (event) => {
    if (event.data === 'tick') {
        release$.next(true);
    } else {
        console.log(`message: ${event.data}`)
    }
}

timerWorker.postMessage({interval: MAGIC_NUMBER})


Rx
    .Observable
    .merge(...fetchFile({...soundsDefinition, ...drumsDefinition, ...bassDefinition}))
    .mergeMap(info => Rx.Observable.combineLatest(Rx.Observable.of(info.name), info.fetch))
    .toArray()
    .subscribe(array => {
        const map = new Map();
        array.forEach(([name, audio]) => map.set(name, {audio}));
        audioMap$.next(map);
        timerWorker.postMessage('start');
    })


//SSE
const sse = new EventSource('http://localhost:8090/stream');

sse.onmessage = (event) => {
    const json = JSON.parse(event.data);
    if (json.action === 'PLAY') {
        processPlay$.next(json)
    } else if (json.action === 'STOP') {
        processStop$.next(json)
    }
}


//high order functions to process music sound
const safePlay = (name) => (map) => (set) => {
    if (!set.has(name)) {
        set.add(name)
        const data = map.get(name)
        data.src = createAudioSource(audioCtx, data.audio)
        console.log(`Start ${name}`)
        data.src.start()
    }
    return [set, map]
}


const safeStop = (name) => (map) => (set) => {
    if (set.has(name)) {
        set.delete(name)
        const data = map.get(name)
        console.log(`Stop ${name}`)
        data.src.stop()
    }
    return [set, map]
}


const applyPlayOrStop = (rsValue) => (audioMapValue) => (fnArray) => {
    return fnArray.reduce(([rs, map], playOrStop) => playOrStop(map)(rs), [rsValue, audioMapValue])
}


//Process rxjs stream
const safePlay$ = processPlay$
    .do(event => console.log(event))
    .concatMap(json => Rx.Observable.of(safePlay(json.name)))

const safeStop$ = processStop$
    .do(event => console.log(event))
    .concatMap(json => Rx.Observable.of(safeStop(json.name)))

Rx
    .Observable
    .merge(safePlay$, safeStop$)
    .buffer(release$)
    .filter(array => array.length > 0)
    .subscribe(fnArray => {
        const rsValue = runningSet$.getValue()
        const audioMapValue = audioMap$.getValue()
        const [newRs, newAudioMap] = applyPlayOrStop(rsValue)(audioMapValue)(fnArray)
        runningSet$.next(newRs)
        audioMap$.next(newAudioMap)
    })