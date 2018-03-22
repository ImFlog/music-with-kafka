import { observable, IObservableArray } from 'mobx';
import TwitterUser from '../beans/TwitterUser';
import TwitterUserEvent from '../beans/TwitterUserEvent';

const sse = new EventSource('http://localhost:8090/users');

class TwitterUserService {
    @observable users: IObservableArray<TwitterUser> = observable([]);

    componentDidMount() {
        sse.onmessage = (event) => {
          const usersEvent: TwitterUserEvent = JSON.parse(event.data);
          usersEvent.users.forEach(user => this.addUser(user));
        }
      }

    addUser(newUser: TwitterUser){
        let user: TwitterUser = this.users.find((user) => user.name === newUser.name );
        if (user !== undefined){
            user.tweetCount = newUser.tweetCount;
        } else {
            this.users.push(newUser);
        }
    }
}

export default new TwitterUserService