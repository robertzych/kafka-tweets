# kafka-tweets

`docker-compose up -d`

`kafka-topics --zookeeper 127.0.0.1 --create -topic twitter_json_01 --partitions 1 --replication-factor 1`

`curl -s -X POST -H 'Content-Type: application/json' --data @connect_twitter.json http://localhost:8083/connectors`

`kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic twitter_json_01 --from-beginning`
