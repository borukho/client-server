package ru.oxothuk;

import org.junit.Ignore;

import java.util.concurrent.TimeUnit;

@Ignore("It's a sample service")
public class EchoService {
    public String echo(String message) {
        return message;
    }
    public void log(String message) {
        System.out.println(message);
    }

    public String waitAndEcho(Long time, String message) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(time);
        return message;
    }
}
