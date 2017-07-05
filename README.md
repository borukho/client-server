[![Build Status](https://travis-ci.org/borukho/client-server.svg?branch=master)](https://travis-ci.org/borukho/client-server)

## Configuration
Service configuration file (`conf/service.properties`) has format
```
<service name>=<class name>
```

To run server use command:
```
java -classpath client-server.jar ServerStarter --port 9119 --service-config conf/service.properties --service-caller-threads 5
```

- `--port <port>` server port to use
- `--service-config <file>` service configuration file location
- `--service-caller-threads <threads count>` threads amount for service caller to use

## Implementation details
Client implementation is `ru.oxothuk.client.Client`. It's thread-safe. 

When calling void service method, `ru.oxothuk.model.VoidResult` is returned in client.

## Logging
Logging is available via Log4J 2.
