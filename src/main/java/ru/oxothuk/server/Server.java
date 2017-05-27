package ru.oxothuk.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.oxothuk.service.Service;
import ru.oxothuk.service.SimpleService;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Server {
    private static Logger logger = LogManager.getLogger(Server.class);
    private ServerConfiguration configuration;
    private Map<String, Service> services;

    public Server(ServerConfiguration configuration) {
        this.configuration = configuration;
        logger.info("Server configuration: {}", configuration);
        services = initServices();
    }

    public void start() {
        //todo start server
    }

    private Map<String, Service> initServices() {
        logger.info("Initializing services");
        return configuration.getServiceConfigurations().stream()
            .map(this::mapToService)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toMap(Service::getName, Function.identity()));
    }

    private Optional<Service> mapToService(ServiceConfiguration configuration) {
        String name = configuration.getName();
        logger.info("Initializing service {}", name);
        try {
            SimpleService service = new SimpleService(configuration);
            logger.info("Service {} initialized", name);
            return Optional.of(service);
        } catch (Exception e) {
            logger.warn("Error while service " + name + " init", e);
        }
        return Optional.empty();
    }

}
