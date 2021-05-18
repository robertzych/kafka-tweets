# kafka-tweets

`docker-compose up -d`

`kafka-topics --zookeeper 127.0.0.1 --create -topic twitter_tweets --partitions 6 --replication-factor 1`

`kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic twitter_tweets`
