package fi.jubic.snoozy.test;

import fi.jubic.snoozy.Application;
import fi.jubic.snoozy.Server;
import fi.jubic.snoozy.ServerConfiguration;
import fi.jubic.snoozy.ServerConfigurator;

import java.net.ServerSocket;

public final class TestUtil {
    /**
     * Starts the application with the given server instance at a automatically
     * selected port on localhost, calls the consumer and finally stops the server.
     */
    public static void withServer(
            Server server,
            Application application,
            ServerConsumer serverConsumer
    ) throws Exception {
        // Select available port
        ServerSocket socket = new ServerSocket(0);

        String hostname = "localhost";
        int port = socket.getLocalPort();

        socket.close();

        // Create configuration
        ServerConfigurator configurator = () -> new ServerConfiguration(hostname, port);

        server.start(application, configurator);

        serverConsumer.consume(hostname, port);

        server.stop();
    }
}
