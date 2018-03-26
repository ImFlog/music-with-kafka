import { observable, IObservableArray } from 'mobx';
import TwitterUser from '../beans/TwitterUser';

const sse = new EventSource('http://localhost:8090/stream/users');

class TwitterUserService {
    @observable users: IObservableArray<TwitterUser> = observable([]);

    constructor() {
        sse.onmessage = (event) => {
            const user: TwitterUser = JSON.parse(event.data);
            this.addUser(user);
        }
    }

    addUser(newUser: TwitterUser) {
        let user: TwitterUser = this.users.find((user) => user.name === newUser.name);
        if (user !== undefined) {
            user.tweetCount = newUser.tweetCount;
        } else {
            this.users.push(newUser);
        }
    }
}

export default new TwitterUserService