package ru.oxothuk.client;

public class ClientException extends Exception {
    ClientException(String message) {
        super(message);
    }

    ClientException(Throwable cause) {
        super(cause);
    }
}
