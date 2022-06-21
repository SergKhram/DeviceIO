FROM openjdk:11
WORKDIR /
ADD target/studio18-1.0-SNAPSHOT.jar app.jar
RUN useradd -m myuser
USER myuser
EXPOSE 8090
CMD java -jar -Dspring.profiles.active=prod app.jar