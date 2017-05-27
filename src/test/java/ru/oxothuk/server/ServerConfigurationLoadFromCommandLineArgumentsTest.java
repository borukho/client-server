package ru.oxothuk.server;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ServerConfigurationLoadFromCommandLineArgumentsTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void assertPortIsEqualToValueFollowingByPortArgument() throws Exception {
        ServerConfiguration configuration = new ServerConfiguration().loadFromCommandLineArguments("--port", "1234");

        assertThat(configuration.getPort(), is(1234));
    }

    @Test
    public void assertHostIsEqualToValueFollowingByHostArgument() throws Exception {
        ServerConfiguration configuration = new ServerConfiguration().loadFromCommandLineArguments("--host", "example.com", "--port", "1234");

        assertThat(configuration.getHost(), is(equalTo("example.com")));
    }

    @Test
    public void ifArgsDoesNotContainHost_thenAssertHostIsLocalhost() throws Exception {
        ServerConfiguration configuration = new ServerConfiguration().loadFromCommandLineArguments("--port", "1234");

        assertThat(configuration.getHost(), is(equalTo("localhost")));
    }

    @Test
    public void ifArgsHasLessThan2_thenAssertExceptionThrown() throws Exception {
        expectedException.expect(allOf(
            is(instanceOf(CommandLineArgumentsConfigurationException.class)),
            hasProperty("message", equalTo("Wrong argument count"))
        ));

        new ServerConfiguration().loadFromCommandLineArguments("--port");
    }

    @Test
    public void ifArgsPortIsNotANumber_thenAssertExceptionThrown() throws Exception {
        expectedException.expect(allOf(
            is(instanceOf(CommandLineArgumentsConfigurationException.class)),
            hasProperty("message", equalTo("Port should be a number: tcp"))
        ));

        new ServerConfiguration().loadFromCommandLineArguments("--port", "tcp");
    }

    @Test
    public void ifNoPortArgument_thenAssertExceptionIsThrown() throws Exception {
        expectedException.expect(allOf(
            is(instanceOf(CommandLineArgumentsConfigurationException.class)),
            hasProperty("message", equalTo("Port number is not set"))
        ));

        new ServerConfiguration().loadFromCommandLineArguments("--help", "--verbose");
    }
}