package org.nuc.revedere.client.connector;

import java.io.Serializable;
import java.net.InetSocketAddress;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class MinaClient {

    private final IoSession session;
    private final IoConnector connector;
    private final MinaHandler handler;

    public MinaClient(String address, int port) throws Exception {
        this.connector = new NioSocketConnector();
        this.handler = new MinaHandler();

        connector.getSessionConfig().setReadBufferSize(2048);
        LoggingFilter restrictedLoggingFilter = new LoggingFilter();
        connector.getFilterChain().addLast("logger", restrictedLoggingFilter);
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
        connector.setHandler(handler);

        if (!validAddress(address)) {
            throw new Exception("Invalid address");
        }

        if (port <= 0) {
            throw new Exception("Invalid port. Expected value bigger than 0, actually it is " + port);
        }

        final ConnectFuture future = connector.connect(new InetSocketAddress(address, port));
        future.awaitUninterruptibly();

        if (!future.isConnected()) {
            throw new Exception("Could not connect");
        }

        session = future.getSession();
        session.getConfig().setUseReadOperation(true);
    }

    public void setHandler(IoHandler handler) {
        this.handler.setHandler(handler);
    }

    public void sendMessage(Serializable message) {
        session.write(message);
    }

    public static boolean validAddress(String address) {
        if (address == null) {
            return false;
        }

        if (address.matches("(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}")) {
            return true;
        } else {
            return false;
        }
    }

    public void removeHandler() {
        this.handler.removeHandler();
    }

    public MinaHandler getHandler() {
        return this.handler;
    }
}
