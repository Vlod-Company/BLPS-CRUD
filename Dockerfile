# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml .
COPY lab/pom.xml lab/pom.xml
COPY laxo-crm-api/pom.xml laxo-crm-api/pom.xml
COPY laxo-crm-ra/pom.xml laxo-crm-ra/pom.xml
COPY paymentService/pom.xml paymentService/pom.xml

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests dependency:go-offline

COPY lab lab
COPY laxo-crm-api laxo-crm-api
COPY laxo-crm-ra laxo-crm-ra
COPY paymentService paymentService

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package

FROM quay.io/wildfly/wildfly:39.0.0.Final-jdk17

USER root
RUN chown -R jboss:jboss /opt/jboss

ENV WILDFLY_HOME=/opt/jboss/wildfly
ENV DEPLOY_DIR=$WILDFLY_HOME/standalone/deployments

RUN mkdir -p /opt/jboss/wildfly/modules/org/postgresql/main && \
    curl -L -o /tmp/postgresql.jar https://jdbc.postgresql.org/download/postgresql-42.6.0.jar && \
    mv /tmp/postgresql.jar /opt/jboss/wildfly/modules/org/postgresql/main/ && \
    chown -R jboss:jboss /opt/jboss/wildfly/modules

RUN mkdir -p /opt/jboss/wildfly/modules/one/laxo/crm/api/main && \
    chown -R jboss:jboss /opt/jboss/wildfly/modules/one

COPY --from=build /workspace/lab/postgresql-module.xml /opt/jboss/wildfly/modules/org/postgresql/main/module.xml
COPY --from=build --chown=jboss:jboss /workspace/laxo-crm-api/target/laxo-crm-api-1.0-SNAPSHOT.jar /opt/jboss/wildfly/modules/one/laxo/crm/api/main/
COPY --from=build --chown=jboss:jboss /workspace/laxo-crm-api/module.xml /opt/jboss/wildfly/modules/one/laxo/crm/api/main/module.xml
COPY --from=build --chown=jboss:jboss /workspace/lab/standalone.xml $WILDFLY_HOME/standalone/configuration/

COPY --from=build --chown=jboss:jboss /workspace/laxo-crm-ra/target/laxo-crm-ra-1.0-SNAPSHOT.rar $DEPLOY_DIR/
COPY --from=build --chown=jboss:jboss /workspace/lab/target/BLPS.war $DEPLOY_DIR/

USER jboss

EXPOSE 8080 9990
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone.xml", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
