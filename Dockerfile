FROM confluentinc/cp-server-connect-operator:6.1.0.0
USER root
RUN echo "===> Installing Twitter connector" \
&& confluent-hub install --no-prompt jcustenborder/kafka-connect-twitter:0.3.33
USER 1001
