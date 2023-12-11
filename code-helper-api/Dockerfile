# Stage 2: Build Final Image
# 复制编译好的 JAR 文件到镜像中
FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/code-helper-api-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8100
EXPOSE 8101
CMD ["java", "-jar", "app.jar"]