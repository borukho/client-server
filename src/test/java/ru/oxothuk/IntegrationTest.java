package ru.oxothuk;

import org.junit.*;
import org.junit.rules.ExpectedException;
import ru.oxothuk.client.Client;
import ru.oxothuk.client.ClientException;
import ru.oxothuk.server.Server;
import ru.oxothuk.server.ServerConfiguration;
import ru.oxothuk.service.ServiceConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class IntegrationTest {
    private static final int PORT = 9119;
    private static final String SERVICE_NAME = "echoService";
    private static final String SERVICE_METHOD = "echo";

    private static Server server;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setUp() throws Exception {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setPort(PORT);
        configuration.setServiceConfigurations(Collections.singletonList(
            new ServiceConfiguration()
                .setName(SERVICE_NAME)
                .setServiceClass(EchoService.class.getCanonicalName())
        ));
        server = new Server(configuration);
        server.start(true);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testSingleRequest() throws Exception {
        Object result;
        try (Client client = new Client("localhost", PORT)) {
            result = client.remoteCall(SERVICE_NAME, SERVICE_METHOD, new Object[]{"hello"});
        }

        assertThat(result, is(equalTo("hello")));
    }

    @Test
    public void testSeveralRequestsOnOneClient() throws Exception {
        int THREADS_COUNT = 2;

        Map<String, Object> results = new HashMap<>();
        try (Client client = new Client("localhost", 9119)) {
            CountDownLatch latch = new CountDownLatch(THREADS_COUNT);
            IntStream.rangeClosed(1, THREADS_COUNT).forEach(i -> {
                String id = "client-" + i;
                Thread thread = new Thread(() -> {
                    try {
                        results.put(id, client.remoteCall(SERVICE_NAME, SERVICE_METHOD, new Object[]{id}));
                    } catch (ClientException e) {
                        throw new RuntimeException(e);
                    } finally {
                        latch.countDown();
                    }
                }, id);
                thread.start();
            });
            latch.await();
        }

        assertThat(results.values(), allOf(
            hasItem(equalTo("client-1")),
            hasItem(equalTo("client-2"))
        ));
    }

    @Test
    public void testNoSuchService() throws Exception {
        exception.expect(allOf(
            is(instanceOf(ClientException.class)),
            hasProperty("message", equalTo("Service no-service not found"))
        ));

        try (Client client = new Client("localhost", PORT)) {
            client.remoteCall("no-service", SERVICE_METHOD, new Object[]{"hello"});
        }
    }

    @Test
    public void testNoSuchMethodByName() throws Exception {
        exception.expect(allOf(
            is(instanceOf(ClientException.class)),
            hasProperty("message", equalTo("Method notAnEcho not found"))
        ));

        try (Client client = new Client("localhost", PORT)) {
            client.remoteCall(SERVICE_NAME, "notAnEcho", new Object[]{"hello"});
        }
    }

    @Test
    public void testNoSuchMethodByParameters() throws Exception {
        exception.expect(allOf(
            is(instanceOf(ClientException.class)),
            hasProperty("message", equalTo("Method echo with such signature not found"))
        ));

        try (Client client = new Client("localhost", PORT)) {
            client.remoteCall(SERVICE_NAME, SERVICE_METHOD, new Object[]{"hello", "world"});
        }
    }
}
