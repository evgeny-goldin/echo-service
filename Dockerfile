FROM evgenyg/java:jre-1.8
MAINTAINER Evgeny Goldin "evgenyg@gmail.com"
ADD build/distributions/echo-service-0.0.1-SNAPSHOT.tar /opt
CMD ["/opt/echo-service-0.0.1-SNAPSHOT/bin/echo-service"]
EXPOSE 8080
