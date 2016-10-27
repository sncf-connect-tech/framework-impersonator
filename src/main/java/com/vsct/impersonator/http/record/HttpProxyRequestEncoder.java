package com.vsct.impersonator.http.record;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;


/**
 * Encodeur de requête HTTP spécifique à un proxy.
 *
 * <p>Quand une requête HTTP est destinée vers un proxy, il faut renseigner l'URI de base du serveur cible, pour que le
 * proxy sache quel serveur contacter.</p>
 *
 * @author pierre_gentile
 * @see org.jboss.netty.handler.codec.http.HttpRequestEncoder
 */
class HttpProxyRequestEncoder extends HttpMessageEncoder {

    /**
     * Space.
     */
    private static final byte SP = 32;

    /**
     * Carriage return.
     */
    private static final byte CR = 13;

    /**
     * Line feed character.
     */
    private static final byte LF = 10;

    /**
     * CRLF.
     */
    private static final byte[] CRLF = new byte[] { CR, LF };

    /**
     * URI de base
     */
    private final String baseUri;

    /**
     * Créer une nouvelle instance.
     *
     * @param serverAddress Adresse du serveur cible
     */
    public HttpProxyRequestEncoder(InetSocketAddress serverAddress) {
        // Générer l'URI de base
        final StringBuilder baseUri = new StringBuilder();
        baseUri.append("http://").append(serverAddress.getHostString());
        if (serverAddress.getPort() != 80) {
            baseUri.append(":").append(serverAddress.getPort());
        }

        this.baseUri = baseUri.toString();
    }

    @Override
    protected void encodeInitialLine(ChannelBuffer buf, HttpMessage message) throws Exception {
        HttpRequest request = (HttpRequest) message;
        buf.writeBytes(request.getMethod().toString().getBytes(StandardCharsets.US_ASCII));
        buf.writeByte(SP);

        buf.writeBytes(baseUri.getBytes(StandardCharsets.US_ASCII));
        buf.writeBytes(request.getUri().getBytes(StandardCharsets.US_ASCII));

        buf.writeByte(SP);
        buf.writeBytes(request.getProtocolVersion().toString().getBytes(StandardCharsets.US_ASCII));
        buf.writeBytes(CRLF);
    }

}