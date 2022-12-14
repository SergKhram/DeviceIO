FROM openjdk:11
WORKDIR /
ADD target/deviceio-1.0.0-SNAPSHOT.jar app.jar
RUN useradd -m myuser
USER myuser
EXPOSE 9900 9901
CMD java "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" -jar -Dspring.profiles.active=production -DinDocker=true -Djava.security.egd=file:/dev/./urandom app.jar