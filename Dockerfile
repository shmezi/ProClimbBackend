FROM openjdk:17
EXPOSE 1200:1200
RUN mkdir /app
COPY ./build/libs/ProClimb Backend-*.jar
ENTRYPOINT ["java","-jar","ProClimbBackend.jar"]