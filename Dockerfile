FROM confluentinc/cp-kafka-connect:6.2.0

RUN echo "===> Installing Twitter connector" \
&& confluent-hub install --no-prompt jcustenborder/kafka-connect-twitter:0.3.33
