# Backup tweet loader
This rust simple script allows you to inject fake tweets into a locally running Kafka instance.

# Building
You need to have rust and cargo installed (look for rustup).
Use `cargo build` and you will find the generated executable in `target/debug/backup_loader`.
You need to copy this executable in `../connect` in order to let it read the tweet template.