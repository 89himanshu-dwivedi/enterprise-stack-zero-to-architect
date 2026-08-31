# Kafka 3.8.1 in KRaft mode - the manual install, written down as a Dockerfile.
FROM eclipse-temurin:17-jdk

ARG KAFKA_VERSION=3.8.1
ARG SCALA_VERSION=2.13

RUN apt-get update \
 && apt-get install -y --no-install-recommends curl ca-certificates \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /opt

# Same link you would click on the Apache Kafka downloads page.
RUN curl -fsSL "https://archive.apache.org/dist/kafka/${KAFKA_VERSION}/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz" -o kafka.tgz \
 && tar -xzf kafka.tgz \
 && mv "kafka_${SCALA_VERSION}-${KAFKA_VERSION}" kafka \
 && rm kafka.tgz

# The PATH trick, so kafka-topics.sh works from any folder inside the container.
ENV PATH="/opt/kafka/bin:${PATH}"

COPY start.sh /opt/start.sh
RUN chmod +x /opt/start.sh

# A clean place to practise when you exec into the container.
WORKDIR /practice

EXPOSE 9092
CMD ["/opt/start.sh"]
