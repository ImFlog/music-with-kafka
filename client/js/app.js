const map = new Map();
const running = new Set();
const audioCtx = new AudioContext();
const eventQueue = [];
const timerWorker = new Worker('../js/worker.js');

function fetchFile(paths) {
    const keys = [...Array(10).keys()]
    const keys2 = [...Array(16).keys()]            
    const drums = keys.map(x => `../audio/drum${x + 1}.ogg`).map(s => fetch(s))
    const bass = keys.map(x => `../audio/bass${x + 1}.ogg`).map(s => fetch(s))
    const sounds = keys2.map(x => `../audio/sounds${x + 1}.ogg`).map(s => fetch(s))
    return { drums, bass, sounds }
}

function loadAudioData(promises) {
    return Promise
            .all(promises)
            .then(response => Promise.all(response.map(r => r.arrayBuffer())))
            .then(buffers => Promise.all(buffers.map(b => audioCtx.decodeAudioData(b))));   
}

function createAudioSource(ctx, data) {
    const bufferSrc = ctx.createBufferSource()
    bufferSrc.buffer = data
    bufferSrc.connect(ctx.destination)
    bufferSrc.loop = true
    bufferSrc.loopStart = 0
    bufferSrc.loopEnd = 4.363900226757369
    return bufferSrc
}


timerWorker.onmessage = (event) => {
    if (event.data === 'tick') {
        while (eventQueue.length) {
            eventQueue.pop()(0)
        }  
    } else {
        console.log(`message: ${event.data}`)
    }
}

timerWorker.postMessage({interval: 4363.900226757})


const { drums, bass, sounds } = fetchFile()

Promise
    .all([loadAudioData(drums), loadAudioData(bass), loadAudioData(sounds)])
    .then(([d, b, s]) => {
        d.forEach((audio, k) => map.set(`drum${k + 1}`, { audio }))
        b.forEach((audio, k) => map.set(`bass${k + 1}`, { audio }))
        s.forEach((audio, k) => map.set(`sound${k + 1}`, { audio }))
        timerWorker.postMessage('start')
    })

const source = new EventSource('http://localhost:8090/stream');

source.onmessage = (event) => {
    console.log(event)
    const json = JSON.parse(event.data)
    const data = map.get(json.name)
    if (json.action === 'PLAY' && !running.has(json.name)) {
        running.add(json.name);
        data.src = createAudioSource(audioCtx, data.audio)        
        eventQueue.push((time) => data.src.start(time))
    } else if (json.action === 'STOP' && running.has(json.name)) {
        running.delete(json.name);
        eventQueue.push((time) => data.src.stop())
    }
};