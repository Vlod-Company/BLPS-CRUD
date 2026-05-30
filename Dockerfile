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

COPY lab/postgresql-module.xml /opt/jboss/wildfly/modules/org/postgresql/main/module.xml
COPY --chown=jboss:jboss laxo-crm-api/target/laxo-crm-api-1.0-SNAPSHOT.jar /opt/jboss/wildfly/modules/one/laxo/crm/api/main/
COPY --chown=jboss:jboss laxo-crm-api/module.xml /opt/jboss/wildfly/modules/one/laxo/crm/api/main/module.xml
COPY --chown=jboss:jboss lab/standalone.xml $WILDFLY_HOME/standalone/configuration/

COPY --chown=jboss:jboss laxo-crm-ra/target/laxo-crm-ra-1.0-SNAPSHOT.rar $DEPLOY_DIR/
COPY --chown=jboss:jboss lab/target/BLPS.war $DEPLOY_DIR/

USER jboss

EXPOSE 8080 9990
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone.xml", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
