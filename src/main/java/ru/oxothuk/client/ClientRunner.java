package ru.oxothuk.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

public class ClientRunner {
    public static void main(String[] args) throws IOException, InterruptedException {
        int THREADS_COUNT = 1;

        try (SimpleClient client = new SimpleClient("localhost", 9119)) {
            CountDownLatch latch = new CountDownLatch(THREADS_COUNT);
            IntStream.rangeClosed(1, THREADS_COUNT).forEach(i -> {
                String threadName = "client-" + i;
                Caller caller = new Caller(client, latch);
                Thread thread = new Thread(caller, threadName);
                thread.start();
            });
            latch.await();
        }
    }
}

class Caller implements Runnable {
    private static Logger logger = LogManager.getLogger(Caller.class);
    private Client client;
    private CountDownLatch latch;

    Caller(Client client, CountDownLatch latch) {
        this.client = client;
        this.latch = latch;
    }

    @Override
    public void run() {
        logger.info("run");
        try {
            Object result = client.remoteCall("service1", "hashCode", new Object[0]);
            logger.info("result: {}", result);
        } catch (ClientException e) {
            logger.warn("error calling service", e);
        } finally {
            latch.countDown();
        }
    }
}
