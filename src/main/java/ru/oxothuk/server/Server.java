package ru.oxothuk.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.oxothuk.service.Service;
import ru.oxothuk.service.ServiceCaller;
import ru.oxothuk.service.ServiceConfiguration;
import ru.oxothuk.service.ServiceLocator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Server implements ServiceLocator, Runnable {
    private static Logger logger = LogManager.getLogger(Server.class);
    private ServerConfiguration configuration;
    private Map<String, Service> services;
    private AtomicInteger counter = new AtomicInteger();
    private boolean stop;
    private ServiceCaller serviceCaller;
    private CountDownLatch startedLatch = new CountDownLatch(1);

    public Server(ServerConfiguration configuration) {
        this.configuration = configuration;
        logger.info("Server configuration: {}", configuration);
        serviceCaller = new ServiceCaller(this, configuration.getServiceCallerThreadCount());
        services = initServices();
    }

    public void start(boolean awaitForStart) throws IOException, InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "shutdown-hook"));
        new Thread(this).start();
        if (awaitForStart) {
            startedLatch.await();
        }
    }

    public void run() {
        Integer port = configuration.getPort();
        logger.info("Listening on port {}", port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            startedLatch.countDown();
            while (!stop) {
                Socket socket = serverSocket.accept();
                String handlerName = "client-handler-" + counter.incrementAndGet();
                Thread thread = new Thread(new ClientHandler(socket, serviceCaller), handlerName);
                thread.start();
            }
        } catch (Exception e) {
            logger.error("error", e);
        }
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
            Service service = new Service(configuration);
            logger.info("Service {} initialized", name);
            return Optional.of(service);
        } catch (Exception e) {
            logger.warn("Error while service " + name + " init", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Service> getServiceByName(String name) {
        if (services.containsKey(name)) {
            return Optional.of(services.get(name));
        }
        return Optional.empty();
    }

    public void stop() {
        if (stop) return;
        stop = true;
        serviceCaller.shutdown();
        Thread.currentThread().interrupt();
    }
}
