package jmri.jmris.json;

/**
 * This is an implementation of a JSON server for JMRI. See
 * {@link jmri.server.json} for more details.
 *
 * @author Paul Bender Copyright (C) 2010
 * @author Randall Wood Copyright (C) 2016
 * @deprecated since 4.19.2; use {@link jmri.server.json.JsonServer} instead
 */
@Deprecated
public class JsonServer extends jmri.server.json.JsonServer {

    /**
     * Create a new server using the default port.
     */
    public JsonServer() {
        super();
    }

    /**
     * Create a new server.
     *
     * @param port    the port to listen on
     * @param timeout the timeout before closing unresponsive connections
     */
    public JsonServer(int port, int timeout) {
        super(port, timeout);
    }
}
