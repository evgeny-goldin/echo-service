FROM evgenyg/java
MAINTAINER Evgeny Goldin "evgenyg@gmail.com"
ADD build/distributions/hello-service-0.0.1-SNAPSHOT.tar /opt
CMD ["/opt/hello-service-0.0.1-SNAPSHOT/bin/hello-service"]
EXPOSE 8080
