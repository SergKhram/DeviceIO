FROM openjdk:11
WORKDIR /
ADD target/deviceio-1.0.0-SNAPSHOT.jar app.jar
RUN useradd -m myuser
USER myuser
EXPOSE 9900 9901
CMD java -jar -Dspring.profiles.active=production app.jar