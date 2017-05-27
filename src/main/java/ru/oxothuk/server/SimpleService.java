package ru.oxothuk.server;

public class SimpleService implements Service {
    private ServiceConfiguration configuration;

    public SimpleService(ServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getName() {
        return configuration.getName();
    }
}
