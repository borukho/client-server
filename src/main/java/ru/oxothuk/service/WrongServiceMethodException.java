package ru.oxothuk.service;

public class WrongServiceMethodException extends ServiceException {
    WrongServiceMethodException(String message) {
        super(message);
    }
}
