FROM eclipse-temurin:21
COPY target/komunumo-*.jar /usr/app/app.jar
RUN groupadd -g 1001 komunumo && useradd -m -u 1001 -g 1001 komunumo
USER komunumo
EXPOSE 8080
CMD ["java", "-jar", "/usr/app/app.jar"]
HEALTHCHECK CMD curl --fail --silent localhost:8080/actuator/health | grep UP || exit 1
