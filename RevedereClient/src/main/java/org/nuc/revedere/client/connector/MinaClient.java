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
import org.nuc.revedere.client.util.NetworkUtils;

public class MinaClient {

    private final IoSession session;
    private final IoConnector connector;
    private final MinaHandler handler;

    public MinaClient(String address) throws Exception {
        this.connector = new NioSocketConnector();
        this.handler = new MinaHandler();

        connector.getSessionConfig().setReadBufferSize(2048);
        LoggingFilter restrictedLoggingFilter = new LoggingFilter();
        connector.getFilterChain().addLast("logger", restrictedLoggingFilter);
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
        connector.setHandler(handler);

        final String ipAddress= NetworkUtils.extractIP(address);;
        final int port = NetworkUtils.extractPort(address);

        final InetSocketAddress remoteAddress = new InetSocketAddress(ipAddress, port);
        final ConnectFuture future = connector.connect(remoteAddress);
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

    

    public void removeHandler() {
        this.handler.removeHandler();
    }

    public MinaHandler getHandler() {
        return this.handler;
    }
}
