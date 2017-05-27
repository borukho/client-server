package ru.oxothuk.client;

public class ClientException extends Throwable {
    public ClientException(String message) {
        super(message);
    }

    public ClientException(Throwable cause) {
        super(cause);
    }
}
