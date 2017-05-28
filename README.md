## Configuration
Service configuration file (`conf/service.properties`) has format
```
<service name>=<class name>
```

To run server use command:
```
java -classpath client-server.jar ServerStarter --port 9119 --service-config conf/service.properties
```

## Implementation details
Client implementation is `ru.oxothuk.client.Client`. It's thread-safe. 

When calling void service method, `ru.oxothuk.model.VoidResult` is returned in client.

## Logging
Logging is available via Log4J 2.
