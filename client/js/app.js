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


const mapping = {
    drum: {
        path: (index) => `../audio/drum${index + 1}.ogg`,
        name: (index) => `drum${index + 1}`
    },
    bass: {
        path: (index) => `../audio/bass${index + 1}.ogg`,
        name: (index) => `bass${index + 1}`
    },
    sound: {
        path: (index) => `../audio/sounds${index + 1}.ogg`,
        name: (index) => `sound${index + 1}`
    }
}


function fetchFile(mapping) {
    const keys = [...Array(10).keys()]
    const keys2 = [...Array(16).keys()]
    
    const drumsName = keys.map(x => [mapping.drum.path(x), mapping.drum.name(x)])
    const bassName = keys.map(x => [mapping.bass.path(x), mapping.bass.name(x)])
    const soundName = keys2.map(x => [mapping.sound.path(x), mapping.sound.name(x)])

    const drums = Rx.Observable.from(drumsName)
        .mergeMap(([filepath, name]) => Rx.Observable.of( { name, fetch: loadAudioData(fetch(filepath))} ));

    const bass = Rx.Observable.from(bassName)
        .mergeMap(([filepath, name]) => Rx.Observable.of( { name, fetch: loadAudioData(fetch(filepath))} ));
    
    const sounds = Rx.Observable.from(soundName)
        .mergeMap(([filepath, name]) => Rx.Observable.of( { name, fetch: loadAudioData(fetch(filepath))} ));

        
    return { drums, bass, sounds }
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


const { drums, bass, sounds } = fetchFile(mapping)

Rx
    .Observable
    .merge(drums, bass, sounds)
    .mergeMap(info => Rx.Observable.combineLatest(Rx.Observable.of(info.name), info.fetch))
    .toArray()
    .subscribe(array => {
        const map = new Map();
        array.forEach(([name, audio]) => map.set(name, {audio}));
        audioMap$.next(map);
        timerWorker.postMessage('start');
    })

const sse = new EventSource('http://localhost:8090/stream');

sse.onmessage = (event) => {
    const json = JSON.parse(event.data);
    if (json.action === 'PLAY') {
        processPlay$.next(json)
    } else if (json.action === 'STOP') {
        processStop$.next(json)
    }
}


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