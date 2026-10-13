FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests

FROM tomcat:11.0.27-jdk25-temurin
RUN rm -rf /usr/local/tomcat/webapps/* \
    && sed -i 's/port="8080"/port="80"/' /usr/local/tomcat/conf/server.xml
COPY --from=build /app/target/proyectoJavaLenguajeMarcas-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
ENV APP_ENV=production
EXPOSE 80
CMD ["catalina.sh", "run"]
