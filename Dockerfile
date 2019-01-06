FROM openjdk:11
ADD log-search-0.0.1-SNAPSHOT.jar /log-search.jar
CMD ["java","-Xms3072m","-Xmx3072m","-jar","log-search.jar"]

# 不要忘记了 暴露端口
#CMD ["java","-server","-XX:+UnlockExperimentalVMOptions","-XX:+UseZGC","-Xms3072m","-Xmx3072m","-XX:MaxDirectMemorySize=512m","-Dcom.sun.management.jmxremote.rmi.port=1099","-Dcom.sun.management.jmxremote=true","-Dcom.sun.management.jmxremote.port=1099","-Dcom.sun.management.jmxremote.ssl=false","-Dcom.sun.management.jmxremote.authenticate=false","-Dcom.sun.management.jmxremote.local.only=false","-Djava.rmi.server.hostname=127.0.0.1","-Xlog:gc*","-jar","application.jar"]
# CMD ["java","-Dcom.sun.management.jmxremote.rmi.port=1099","-Dcom.sun.management.jmxremote=true","-Dcom.sun.management.jmxremote.port=1099","-Dcom.sun.management.jmxremote.ssl=false","-Dcom.sun.management.jmxremote.authenticate=false","-Dcom.sun.management.jmxremote.local.only=false","-Djava.rmi.server.hostname=127.0.0.1","-jar","application.jar"]