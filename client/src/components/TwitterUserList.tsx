import * as React from "react";
import { observer } from "mobx-react";
import TwitterUser from "../beans/TwitterUser";

export interface TwitterUserListProps {
  twitterUsers: TwitterUser[]
}

@observer
export default class TwitterUserList extends React.Component<TwitterUserListProps, undefined> {

  render() {
    const users = this.props.twitterUsers.sort((user1, user2) => user1.compare(user2)).slice(0, 5).map((user, idx) => {
      const key = user.name + idx;
      return (
        <tr>
          <th style={thStyle}><img src={user.imgUri} height="70" width="70" /></th>
          <th style={thStyle}>{user.name}</th>
          <th style={thStyle}>{user.tweetCount}</th>
        </tr>
      )
    });
    return (
      <table >
        {users}
      </table>
    );
  }

}


const thStyle = {
  padding: '15px'
}