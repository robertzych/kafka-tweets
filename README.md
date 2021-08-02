# kafka-tweets

`docker-compose up -d`
`kafka-topics --zookeeper 127.0.0.1 --create -topic twitter_json_01 --partitions 1 --replication-factor 1`
`curl -s -X POST -H 'Content-Type: application/json' --data @connect_file_source.json http://localhost:8083/connectors`
`curl http://localhost:8083/connectors/file_source/status`
`kafkacat -b 127.0.0.1:9092 -t twitter_json_01 -C -e -q | wc -l`
`kafkacat -b 127.0.0.1:9092 -t kafka_users -C -e -q | wc -l`
`curl -X DELETE http://localhost:8083/connectors/file_source`
`curl http://localhost:8083/connectors`
`curl -s -X POST -H 'Content-Type: application/json' --data @connect_twitter.json http://localhost:8083/connectors`
`curl http://localhost:8083/connectors/twitter_source/status`
`kafkacat -b 127.0.0.1:9092 -t twitter_json_01 -C -e -q | wc -l`
`kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic twitter_json_01 --from-beginning`
`kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic kafka_users --from-beginning`
`curl -s -X POST -H 'Content-Type: application/json' --data @connect_file_sink.json http://localhost:8083/connectors`
`curl http://localhost:8083/connectors/file_sink/status`
`curl -s -X POST -H 'Content-Type: application/json' --data @connect_file_sink_filtered.json http://localhost:8083/connectors`
`curl http://localhost:8083/connectors/file_sink_filtered/status`
`wc -l data/kafka_tweets_01.txt`
`tail -f data/kafka_tweets_01.txt | jq '.Text'`
`curl -s -X POST -H 'Content-Type: application/json' --data @connect_neo4j.json http://localhost:8083/connectors`
`curl http://localhost:8083/connectors/neo4j_sink/status`
`MATCH (t:Tweet) RETURN count(t)`
`CALL db.schema.visualization()`
`head -n 1000 kafka_tweets_03_filtered.txt | sed -E "s|Id\":([0-9]+)|Id\":\"\1\"|g" | jq --raw-output '"\(.Id)^^\(.Community)^^\(.Text)~~"' | tr '\n' ' ' | sed 's/~~/\n/g' > filtered_tweets_with_no_labels.csv`

[//]: # (TODO: create a topic that doesn't include any franz kafka tweets)
[//]: # (TODO: create a topic that only contains new users)
[//]: # (TODO: consider creating a Neo4j custom Docker image that contains both APOC and GDS so it doesn't need to download each time `docker-compose` is run)
[//]: # (TODO: wait for Neo4j to spin-up before Connect attempts to send messages to it)
[//]: # (TODO: consider removing `twitter-producer` and Gradle stuff because this is now being done by Connect)
[//]: # (TODO: include a narrative in README.md that shows what's happening, where to put credentials, etc... Add diagrams)
[//]: # (TODO: use Gunnar's new kafka connect cli tool instead of curl)