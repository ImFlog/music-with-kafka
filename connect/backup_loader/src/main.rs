extern crate rand;

use std::time::{SystemTime, UNIX_EPOCH};
use std::{thread, time};
use std::error::Error;
use std::fs::File;
use std::io::prelude::*;
use std::path::Path;
use std::process::Command;

const SOURCES: [&'static str; 3] = ["iphone", "web", "android"];
const TEXTS: [&'static str; 3] = [
    "Give me some drum #musicwithkafka",
    "I want bass #musicwithkafka",
    "How about other sound #musicwithkafka",
];
const USERS: [(&'static str, &'static str); 10] = [
    ("monty", "https://static.boredpanda.com/blog/wp-content/uploads/2014/11/most-popular-cats-monty-2.jpg"),
    ("honey_bee", "https://static.boredpanda.com/blog/wp-content/uploads/2014/11/most-popular-cats-honey-bee__605.jpg"),
    ("venus", "https://static.boredpanda.com/blog/wp-content/uploads/2014/11/most-popular-cats-venus-21.jpg"),
    ("lil_bub", "https://static.boredpanda.com/blog/wp-content/uploads/2014/11/most-popular-cats-lil-bub-11__605.jpg"),
    ("maru", "https://static.boredpanda.com/blog/wp-content/uploads/2014/11/famous-internet-cats-201__605.jpg"),
    ("grumpycat", "https://static.boredpanda.com/blog/wp-content/uploads/2014/11/most-popular-cats-grumpy-cat-11__605.jpg"),
    ("garfy", "https://static.boredpanda.com/blog/wp-content/uploads/2014/11/garfi-evil-grumpy-persian-cat-12.jpg"),
    ("shironeko", "https://static.boredpanda.com/blog/wp-content/uploads/2014/11/shironeko-happy-cat-11.jpg"),
    ("snoopy", "https://static.boredpanda.com/blog/wp-content/uploads/2014/11/most-popular-cats-snoopy-11__605.jpg"),
    ("hamilton", "https://static.boredpanda.com/blog/wp-content/uploads/2014/11/most-popular-cats-hamilton-11__605.jpg")];

fn main() {
    loop {
        let tweet = build_tweet();
        send_kafka_message(tweet);

        // Wait between messages
        let sec = time::Duration::from_secs(1);
        thread::sleep(sec);
    }
}

// Retrieve tweet template and replace variables values
fn build_tweet() -> String {
    let mut template = read_template_content();

    // Replace variable parts
    match SystemTime::now().duration_since(UNIX_EPOCH) {
        Ok(n) => template = template.replace("${date}", &n.as_secs().to_string()),
        Err(_) => panic!("SystemTime before UNIX EPOCH!"),
    };

    let random = rand::random::<usize>();
    let source = random % SOURCES.len();
    let user = random % USERS.len();
    let text = random % TEXTS.len();

    template = template.replace("${source}", SOURCES[source]);
    template = template.replace("${text}", TEXTS[text]);
    template = template.replace("${handler}", USERS[user].0);
    template = template.replace("${imageUrl}", USERS[user].1);
    return template;
}

fn read_template_content() -> String {
    // Read template content
    let path = Path::new("tweets_backup_template.json");
    let display = path.display();
    let mut file = match File::open(&path) {
        Err(why) => panic!("couldn't open {}: {}", display, why.description()),
        Ok(file) => file,
    };
    let mut s = String::new();
    match file.read_to_string(&mut s) {
        Err(why) => panic!("couldn't read {}: {}", display, why.description()),
        Ok(_) => (),
    };
    return s;
}

fn send_kafka_message(tweet: String) {
    let cmd = format!("echo \"{}\" | $KAFKA_HOME/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic twitter_json", tweet);
    let output = Command::new("sh")
        .arg("-c")
        .arg(cmd)
        .output()
        .expect("failed to execute process");
    println!("sending {}", tweet);
    println!("status: {}", output.status);
    println!("stderr: {}", String::from_utf8_lossy(&output.stderr));
}
