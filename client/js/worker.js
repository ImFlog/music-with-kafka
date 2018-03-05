
let timerID = null;
let interval = 100

self.onmessage = (e) => {
	if (e.data === 'start') {
		console.log("starting");
		timerID = setInterval(() => postMessage('tick'), interval)
	} else if (e.data.interval) {
		interval = e.data.interval;
		if (timerID) {
			clearInterval(timerID);
			timerID = setInterval(() => postMessage('tick'), interval)
		}
	} else if (e.data === 'stop') {
		clearInterval(timerID);
		timerID = null;
	}
};

postMessage('hi there');