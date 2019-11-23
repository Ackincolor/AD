FROM azul/zulu-openjdk-alpine:11
WORKDIR /source
COPY target/video-dispatcher-1.0-SNAPSHOT.jar /source/app.jar
COPY ssl/keystore.pkcs12 /source/ssl/keystore.pkcs12
COPY video-key.json /source/ArchiDistri.json
ENV GOOGLE_APPLICATION_CREDENTIALS=key.json
RUN chmod 777 -R /source
EXPOSE 42308
CMD ["/usr/bin/java","-jar","-Dspring.profiles.active=default","/source/app.jar"]

