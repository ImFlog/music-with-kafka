import * as React from "react";
import {observer} from "mobx-react";
import TwitterUser from "../beans/TwitterUser";

export interface TwitterUserListProps {
  twitterUsers: TwitterUser[]
}

@observer
export default class TwitterUserList extends React.Component<TwitterUserListProps, undefined> {

  render(){
    const users = this.props.twitterUsers.sort((user1, user2) => user1.compare(user2)).map((user, idx) => {
    const key = user.name + idx;
    return (
      <div key={key}><img src={user.imgUri} height="70" /> {user.name} - {user.tweetCount}</div>
    )});
    return (
      <div>
        {users}
      </div>
    );
  }

}
