package ru.oxothuk.client;

public interface Client {
    Object remoteCall(String serviceName, String methodName, Object[] parameters);
}
