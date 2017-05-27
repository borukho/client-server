package ru.oxothuk.server;

import ru.oxothuk.client.Response;

public interface ResponseCallback {
    void callback(Response response);
}
