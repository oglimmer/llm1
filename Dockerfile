FROM eclipse-temurin:17-jdk-ubi9-minimal

WORKDIR /app

COPY . .

RUN ./mvnw package

EXPOSE 8888

ENV SOURCE_CODE_ROOT=/mnt/source_code
ENV OPENAI_KEY=PUT_KEY_HERE

CMD ["java", "-jar", "./target/llm1-0.0.1-SNAPSHOT.jar"]
