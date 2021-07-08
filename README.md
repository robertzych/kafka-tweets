# kafka-tweets

`docker-compose up -d`
`curl http://localhost:8083/connectors`
`kafka-topics --zookeeper 127.0.0.1 --create -topic twitter_json_01 --partitions 1 --replication-factor 1`
`curl -s -X POST -H 'Content-Type: application/json' --data @connect_twitter.json http://localhost:8083/connectors`
`curl http://localhost:8083/connectors/twitter_source/status`
`curl -X DELETE http://localhost:8083/connectors/twitter_source`
`curl -s -X POST -H 'Content-Type: application/json' --data @connect_file_sink.json http://localhost:8083/connectors`
`curl http://localhost:8083/connectors/file_sink/status`
`curl -s -X POST -H 'Content-Type: application/json' --data @connect_file_source.json http://localhost:8083/connectors`
`curl http://localhost:8083/connectors/file_source/status`
`curl -s -X POST -H 'Content-Type: application/json' --data @connect_neo4j.json http://localhost:8083/connectors`
`curl http://localhost:8083/connectors/neo4j_sink/status`
`curl -X DELETE http://localhost:8083/connectors/neo4j_sink`
`kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic twitter_json_01 --from-beginning`
`kafkacat -b 127.0.0.1:9092 -t twitter_json_01 -C -e -q | wc -l`

[//]: # (TODO: figure out a way to retain twitter_json_01 even after `docker-compose down`)
[//]: # (TODO: no data in neo4j)
[//]: # (TODO: consider creating a Neo4j custom Docker image that contains both APOC and GDS so it doesn't need to download each time `docker-compose` is run)
[//]: # (TODO: wait for Neo4j to spin-up before Connect attempts to send messages to it)
[//]: # (TODO: consider removing `twitter-producer` and Gradle stuff because this is now being done by Connect)
[//]: # (TODO: include a narrative in README.md that shows what's happening, where to put credentials, etc... Add diagrams)