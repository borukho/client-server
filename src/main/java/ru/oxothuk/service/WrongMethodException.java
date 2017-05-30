package ru.oxothuk.service;

public class WrongMethodException extends ServiceException {
    public WrongMethodException(String message) {
        super(message);
    }
}
