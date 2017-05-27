package ru.oxothuk.service;

import ru.oxothuk.server.ServiceConfiguration;

public class SimpleService implements Service {
    private ServiceConfiguration configuration;

    public SimpleService(ServiceConfiguration configuration) {
        this.configuration = configuration;
        try {
            Class.forName(configuration.getServiceClass());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return configuration.getName();
    }
}
