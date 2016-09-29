FROM java:8-jre
ADD ./target/leadership-election.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/leadership-election.jar"]
