import TwitterUser from "./TwitterUser";

export default class TwitterUserEvent {
    users: TwitterUser[];
  
    constructor(users: TwitterUser[]) {
      this.users = users;
    }
  }