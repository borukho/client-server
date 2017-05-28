package ru.oxothuk.server;

import ru.oxothuk.model.Response;

public interface ResponseCallback {
    void callback(Response response);
}
