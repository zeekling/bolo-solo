FROM docker.io/library/maven:3.9.11-amazoncorretto-21-al2023 as MVN_BUILD

WORKDIR /opt/bolo/
ADD . /tmp
RUN cd /tmp && mvn package -DskipTests -Pci && mv target/bolo/* /opt/bolo/ \
    && cp -f /tmp/src/main/resources/docker/* /opt/bolo/WEB-INF/classes/

FROM openjdk:26-ea-21-slim
LABEL maintainer="Liang Ding<d@b3log.org>"

WORKDIR /opt/bolo/
COPY --from=MVN_BUILD /opt/bolo/ /opt/bolo/
RUN apt-get update && apt-get install -y ca-certificates tzdata

ENV TZ=Asia/Shanghai
EXPOSE 8080

ENTRYPOINT [ "java", "-cp", "WEB-INF/lib/*:WEB-INF/classes", "org.b3log.solo.Starter" ]
