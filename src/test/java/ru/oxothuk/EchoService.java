package ru.oxothuk;

import org.junit.Ignore;

@Ignore("It's a sample service")
public class EchoService {
    public String echo(String message) {
        return message;
    }
}
