package org.nuc.revedere.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.nuc.revedere.client.connector.MinaClient;
import org.nuc.revedere.client.connector.MinaHandler;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.request.Request;
import org.nuc.revedere.util.Container;

public class MinaRequestor<T extends Request> {
    private static final Logger LOGGER = Logger.getLogger(MinaRequestor.class);
    private static final String TIMEOUT_EXCEPTION_MESSAGE = "Timeout while waiting for response";
    private final MinaHandler minaHandler;
    private final MinaClient minaClient;

    public MinaRequestor(MinaClient minaClient) {
        this.minaHandler = minaClient.getHandler();
        this.minaClient = minaClient;
    }

    public Response<T> request(final T request) {
        final Container<Response<T>> responseContainer = new Container<>();
        final CountDownLatch latch = new CountDownLatch(1);
        minaHandler.setHandler(new IoHandlerAdapter() {
            @SuppressWarnings("unchecked")
            @Override
            public void messageReceived(IoSession session, Object message) {
                try {
                    final Response<T> possibleResponse = (Response<T>) message;
                    if (possibleResponse.getRequest().equals(request)) {
                        responseContainer.setContent(possibleResponse);
                        latch.countDown();
                    }
                } catch (ClassCastException e) {
                    // ignore this message
                }
            }
        });

        this.minaClient.sendMessage(request);
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                responseContainer.setContent(generateTimeoutResponse(request));
            }
        } catch (Exception e) {
            LOGGER.error("Could not wait for response", e);
            responseContainer.setContent(generateTimeoutResponse(request));
        }
        minaClient.setHandler(null);
        return responseContainer.getContent();
    }

    private Response<T> generateTimeoutResponse(final T request) {
        return new Response<T>(request, false, TIMEOUT_EXCEPTION_MESSAGE);
    }
}
