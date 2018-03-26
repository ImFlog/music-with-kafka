import { observable, IObservableArray } from 'mobx';
import TwitterUser from '../beans/TwitterUser';

class TwitterUserService {
    @observable users: IObservableArray<TwitterUser> = observable([]);

    addUser(newUser: TwitterUser) {
        newUser = new TwitterUser(newUser.name, newUser.imgUri, newUser.tweetCount);
        let user: TwitterUser = this.users.find((user) => user.name === newUser.name);
        if (user !== undefined) {
            user.tweetCount = newUser.tweetCount;
        } else {
            this.users.push(newUser);
        }
    }
}

export default new TwitterUserService