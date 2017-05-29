package ru.oxothuk.service;

class ServiceException extends Exception {
    ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
