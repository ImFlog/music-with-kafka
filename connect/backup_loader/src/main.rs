extern crate rdkafka;
extern crate rand;

use std::time::{Duration, SystemTime, UNIX_EPOCH};
use std::{thread, time};
use std::error::Error;
use std::fs::File;
use std::io::prelude::*;
use std::path::Path;

use rdkafka::config::ClientConfig;
use rdkafka::producer::FutureProducer;
use rdkafka::util::get_rdkafka_version;


const SOURCES: [&'static str; 3] = ["iphone", "web", "android"];
const TEXTS: [&'static str; 9] = [
    "Give me some drum #musicwithkafka",
    "I want some heavy bass #musicwithkafka",
    "I want some lead_bass #musicwithkafka",
    "I want some line bass #musicwithkafka",
    "How about a melody ? #musicwithkafka",
    "And a pad ? #musicwithkafka",
    "And synth ? #musicwithkafka",
    "Vocal ftw ! #musicwithkafka",
    "Osef ftw ! #musicwithkafka",
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
    let template = read_template_content();

    let producer: FutureProducer = ClientConfig::new()
        .set("bootstrap.servers", "localhost:9092")
        .set("message.timeout.ms", "1000")
        .create()
        .expect("Producer creation error");

    // Counter for ids
    let mut count = 0;
    loop {
        let tweet = build_tweet(template.to_owned(), count);
        let key = format!("{{\"Id\": {}}}", count);
        count = count + 1;

        let time = SystemTime::now().duration_since(UNIX_EPOCH)
            .unwrap();

        producer.send_copy("twitter_json", None, Some(&tweet), Some(&key), time, 0)
            .map(move |delivery_status| {   // This will be executed once the result is received
                info!("Delivery status for message {} received", i);
                delivery_status
        });

        // Wait between messages
        let sec = time::Duration::from_secs(1);
        thread::sleep(sec);
    }
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

// Retrieve tweet template and replace variables values
fn build_tweet(template: String, id: u64) -> String {
    let mut result = template;
    // Replace variable parts
    match SystemTime::now().duration_since(UNIX_EPOCH) {
        Ok(n) => result = result.replace("${date}", &n.as_secs().to_string()),
        Err(_) => panic!("SystemTime before UNIX EPOCH!"),
    };

    let random = rand::random::<usize>();
    let source = random % SOURCES.len();
    let user = random % USERS.len();
    let text = random % TEXTS.len();

    result = result.replace("${id}", &(id.to_string()));
    result = result.replace("${source}", SOURCES[source]);
    result = result.replace("${text}", TEXTS[text]);
    result = result.replace("${handler}", USERS[user].0);
    result = result.replace("${imageUrl}", USERS[user].1);
    return result;
}
