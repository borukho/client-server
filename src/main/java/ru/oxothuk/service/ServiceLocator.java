package ru.oxothuk.service;

import java.util.Optional;

public interface ServiceLocator {
    Optional<Service> getServiceByName(String name);
}
