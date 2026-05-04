# jeecg-boot 后端 Docker 镜像（两阶段构建）
# 阶段1：编译
FROM maven:3.8.6-eclipse-temurin-17 AS builder

WORKDIR /build

# 复制项目文件
COPY jeecg-boot/jeecg-boot /build

# 设置 Maven 阿里云镜像
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > /root/.m2/settings.xml && \
    echo '<settings>' >> /root/.m2/settings.xml && \
    echo '  <mirrors>' >> /root/.m2/settings.xml && \
    echo '    <mirror>' >> /root/.m2/settings.xml && \
    echo '      <id>aliyun</id>' >> /root/.m2/settings.xml && \
    echo '      <mirrorOf>central</mirrorOf>' >> /root/.m2/settings.xml && \
    echo '      <name>Aliyun Maven</name>' >> /root/.m2/settings.xml && \
    echo '      <url>https://maven.aliyun.com/repository/public</url>' >> /root/.m2/settings.xml && \
    echo '    </mirror>' >> /root/.m2/settings.xml && \
    echo '  </mirrors>' >> /root/.m2/settings.xml && \
    echo '</settings>' >> /root/.m2/settings.xml

# 编译项目
RUN mvn clean package -DskipTests -Dmaven.test.skip=true

# 阶段2：运行
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 从构建阶段复制 jar 包
COPY --from=builder /build/jeecg-module-system/jeecg-system-start/target/jeecg-system-start-*.jar /app/app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
