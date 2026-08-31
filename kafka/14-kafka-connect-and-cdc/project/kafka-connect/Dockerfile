# A Kafka Connect worker is just a JVM with connector jars on its plugin path.
# This image resolves those jars with Maven in a build stage, then copies each
# connector into its own folder under the plugin path.

FROM maven:3.9-eclipse-temurin-17 AS plugins

WORKDIR /build
COPY pom.xml .

# resolve every dependency declared in pom.xml into /build/target/connectors
RUN mvn -B -q dependency:copy-dependencies \
        -DoutputDirectory=/build/target/connectors \
        -DincludeScope=runtime

# ---------------------------------------------------------------------------

FROM confluentinc/cp-kafka-connect:7.7.1

USER root

# Each connector gets its own directory. Connect isolates classloaders per
# plugin directory - mixing jars from two connectors in one folder is the
# classic "NoSuchMethodError at startup" trap.
RUN mkdir -p /usr/share/confluent-hub-components/kafka-connect-jdbc/lib \
             /usr/share/confluent-hub-components/kafka-connect-s3/lib \
             /usr/share/confluent-hub-components/debezium-postgres/lib

COPY --from=plugins /build/target/connectors/kafka-connect-jdbc*.jar \
                    /usr/share/confluent-hub-components/kafka-connect-jdbc/lib/
COPY --from=plugins /build/target/connectors/postgresql*.jar \
                    /usr/share/confluent-hub-components/kafka-connect-jdbc/lib/

COPY --from=plugins /build/target/connectors/kafka-connect-s3*.jar \
                    /usr/share/confluent-hub-components/kafka-connect-s3/lib/
COPY --from=plugins /build/target/connectors/aws*.jar \
                    /usr/share/confluent-hub-components/kafka-connect-s3/lib/

COPY --from=plugins /build/target/connectors/debezium*.jar \
                    /usr/share/confluent-hub-components/debezium-postgres/lib/
COPY --from=plugins /build/target/connectors/protobuf*.jar \
                    /usr/share/confluent-hub-components/debezium-postgres/lib/

USER appuser
