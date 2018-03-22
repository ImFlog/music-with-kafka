import { observable } from "mobx";

export default class TwitterUser {
  name: string;
  @observable tweetCount: number;
  imgUri: string;

  constructor(name: string, imgUri: string, tweetCount: number) {
    this.name = name;
    this.imgUri = imgUri;
    this.tweetCount = tweetCount;
  }

  compare(user: TwitterUser): number {
    if (this.tweetCount < user.tweetCount) {
      return 1;
    }
    if (this.tweetCount > user.tweetCount) {
      return -1;
    }
    return 0;
  }
}