# kafka-tweets

## CLI Commands
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

`wc -l data/kafka_tweets_03_predicted_1500.txt`

`tail -f data/kafka_tweets_01.txt | jq '.Text'`

`curl -s -X POST -H 'Content-Type: application/json' --data @connect_neo4j.json http://localhost:8083/connectors`

`curl http://localhost:8083/connectors/neo4j_sink/status`

`MATCH (t:Tweet) RETURN count(t)`

`CALL db.schema.visualization()`

`head -n 1000 kafka_tweets_03_filtered.txt | sed -E "s|Id\":([0-9]+)|Id\":\"\1\"|g" | jq --raw-output '"\(.Id)^\(.Community)^\(.Text)~~"' | tr '\n' ' ' | sed 's/~~/\n/g' > filtered_tweets_with_no_labels.csv`

`cat kafka_tweets_03_predicted_1500.txt | sed -E "s|Id\":([0-9]+)|Id\":\"\1\"|g" | jq --raw-output '"\(.Id)^\(.community)^\(.Text)~~"' | tr '\n' ' ' | sed 's/~~/\n/g' > kafka_tweets_03_predicted_1500.csv`

## gke setup

`docker build -t gcr.io/fresh-delight-322120/cp-server-connect-operator:6.1.0.0-twitter .`

`docker push gcr.io/fresh-delight-322120/cp-server-connect-operator:6.1.0.0-twitter`

`gcloud container clusters create kafka-streams-cluster --num-nodes 3 --machine-type e2-standard-2 --zone us-west4-a`

`gcloud container clusters get-credentials kafka-streams-cluster`

`kubectl create namespace confluent`

`kubectl config set-context --current --namespace=confluent`

`helm repo add confluentinc https://packages.confluent.io/helm`

`helm upgrade --install operator confluentinc/confluent-for-kubernetes`

`openssl genrsa -out ca-key.pem 2048`

`openssl req -new -key ca-key.pem -x509 -days 1000 -out $TUTORIAL_HOME/ca.pem -subj "/C=US/ST=CA/L=MountainView/O=Confluent/OU=Operator/CN=TestCA"`

`kubectl create secret tls ca-pair-sslcerts --cert=$TUTORIAL_HOME/ca.pem --key=ca-key.pem`

`kubectl create secret generic cloud-plain --from-file=plain.txt=creds-client-kafka-sasl-user.txt`

`kubectl create secret generic cloud-sr-access --from-file=basic.txt=creds-schemaRegistry-user.txt`

`kubectl create secret generic control-center-user --from-file=basic.txt=creds-control-center-users.txt`

`kubectl apply -f confluent-platform.yaml`

`kubectl get pods` should return 4 pods (confluent-operator-*, connect-0, controlcenter-0, streams-classifier-0) 

`kubectl get events` good for debugging and monitoring  

`kubectl port-forward controlcenter-0 9021:9021` and open https://localhost:9021 (login: admin/Developer1) 

## gke teardown

`kubectl delete -f confluent-platform.yaml`

`helm uninstall confluent-operator`

`kubectl delete secrets cloud-plain cloud-sr-access control-center-user`

`kubectl delete secret ca-pair-sslcerts`

`gcloud container clusters delete kafka-streams-cluster` 

[//]: # (TODO: send predictions to another topic for performance monitoring)
[//]: # (TODO: consider creating a Neo4j custom Docker image that contains both APOC and GDS so it doesn't need to download each time `docker-compose` is run)
[//]: # (TODO: wait for Neo4j to spin-up before Connect attempts to send messages to it)
[//]: # (TODO: consider removing `twitter-producer` and Gradle stuff because this is now being done by Connect)
[//]: # (TODO: include a narrative in README.md that shows what's happening, where to put credentials, etc... Add diagrams)
[//]: # (TODO: use Gunnar's new kafka connect cli tool instead of curl)