FROM openjdk:8-jre-alpine

ENV APP_SLEEP=0 \
    JAVA_OPTS=""

ADD *.war /app.war

EXPOSE 8080
CMD echo "The application will start in ${APP_SLEEP}s..." && \
    sleep ${APP_SLEEP} && \
    java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.war