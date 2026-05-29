FROM quay.io/wildfly/wildfly:39.0.0.Final-jdk17

USER root
RUN chown -R jboss:jboss /opt/jboss

ENV WILDFLY_HOME /opt/jboss/wildfly
ENV DEPLOY_DIR $WILDFLY_HOME/standalone/deployments

RUN mkdir -p /opt/jboss/wildfly/modules/org/postgresql/main && \
    curl -L -o /tmp/postgresql.jar https://jdbc.postgresql.org/download/postgresql-42.6.0.jar && \
    mv /tmp/postgresql.jar /opt/jboss/wildfly/modules/org/postgresql/main/ && \
    chown -R jboss:jboss /opt/jboss/wildfly/modules

COPY postgresql-module.xml /opt/jboss/wildfly/modules/org/postgresql/main/module.xml

COPY standalone.xml $WILDFLY_HOME/standalone/configuration/

COPY target/BLPS.war $DEPLOY_DIR/

USER jboss

EXPOSE 8080 9990
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone.xml", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]