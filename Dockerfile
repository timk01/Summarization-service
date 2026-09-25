FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY certificates/Russian_Trusted_Root_CA.cer /tmp/Russian_Trusted_Root_CA.cer
COPY certificates/Russian_Trusted_Sub_CA.cer /tmp/Russian_Trusted_Sub_CA.cer
COPY certificates/Russian_Trusted_Sub_CA_2024.cer /tmp/Russian_Trusted_Sub_CA_2024.cer

RUN keytool -importcert -noprompt -trustcacerts \
        -alias russian-trusted-root \
        -file /tmp/Russian_Trusted_Root_CA.cer \
        -keystore "$JAVA_HOME/lib/security/cacerts" \
        -storepass changeit \
    && keytool -importcert -noprompt -trustcacerts \
        -alias russian-trusted-sub \
        -file /tmp/Russian_Trusted_Sub_CA.cer \
        -keystore "$JAVA_HOME/lib/security/cacerts" \
        -storepass changeit \
    && keytool -importcert -noprompt -trustcacerts \
        -alias russian-trusted-sub-2024 \
        -file /tmp/Russian_Trusted_Sub_CA_2024.cer \
        -keystore "$JAVA_HOME/lib/security/cacerts" \
        -storepass changeit \
    && rm /tmp/*.cer

COPY --from=build /app/build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]