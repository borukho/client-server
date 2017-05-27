package ru.oxothuk.service;

public class Service {
    private ServiceConfiguration configuration;
    private final Object target;

    public Service(ServiceConfiguration configuration) {
        this.configuration = configuration;
        try {
            target = Class.forName(configuration.getServiceClass()).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return configuration.getName();
    }
}
