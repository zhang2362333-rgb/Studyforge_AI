FROM tomcat:10.1-jdk17-temurin

ENV TZ=Asia/Shanghai \
    STUDYFORGE_UPLOAD_DIR=/var/lib/studyforge/uploads

RUN rm -rf /usr/local/tomcat/webapps/* \
    && mkdir -p /var/lib/studyforge/uploads/images

COPY studyforge-server/studyforge-webapi/target/studyforge-webapi-1.0.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
