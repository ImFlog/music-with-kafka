#!/bin/bash
# Build the connector
cd kafka-connect-twitter && mvn -DskipTests clean package

# Export the jar in current env var
export CLASSPATH="$(find kafka-connect-twitter/target/kafka-connect-target/usr/share/kafka-connect/kafka-connect-twitter/ -type f -name '*.jar' | tr '\n' ':')"
echo $CLASSPATH

# Start kafka-connect with current twitter.properties
$KAFKA_HOME/bin/connect-standalone.sh $KAFKA_HOME/config/connect-standalone.properties twitter.properties
